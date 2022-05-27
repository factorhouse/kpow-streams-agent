(ns io.operatr.kpow.agent
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.core.protocols :as p])
  (:import (java.util UUID)
           (java.util.concurrent Executors TimeUnit ThreadFactory)
           (org.apache.kafka.clients.producer Producer ProducerRecord)
           (org.apache.kafka.streams Topology KeyValue TopologyDescription TopologyDescription$Subtopology
                                     TopologyDescription$GlobalStore TopologyDescription$Node TopologyDescription$Source
                                     TopologyDescription$Processor TopologyDescription$Sink)
           (java.util.concurrent.atomic AtomicInteger)))

(def kpow-snapshot-topic
  {:topic "__oprtr_snapshot_state"})

(extend-protocol p/Datafiable
  KeyValue
  (datafy [kv]
    {:key   (.key kv)
     :value (.value kv)})

  TopologyDescription
  (datafy [td]
    {:sub-topologies (set (map p/datafy (.subtopologies td)))
     :global-stores  (set (map p/datafy (.globalStores td)))})

  TopologyDescription$Subtopology
  (datafy [st]
    {:id    (.id st)
     :nodes (set (map p/datafy (.nodes st)))})

  TopologyDescription$GlobalStore
  (datafy [gs]
    {:id        (.id gs)
     :source    (p/datafy (.source gs))
     :processor (p/datafy (.processor gs))})

  TopologyDescription$Node
  (datafy [node]
    (cond-> {:name         (.name node)
             :predecessors (set (map #(identity {:name (.name %)}) (.predecessors node)))
             :successors   (set (map #(identity {:name (.name %)}) (.successors node)))}
      (instance? TopologyDescription$Source node)
      (merge {:topic-set     (into #{} (.topicSet ^TopologyDescription$Source node))
              :topic-pattern (some-> (.topicPattern ^TopologyDescription$Source node) str)})
      (instance? TopologyDescription$Processor node)
      (assoc :stores (.stores ^TopologyDescription$Processor node))
      (instance? TopologyDescription$Sink node)
      (merge {:topic             (.topic ^TopologyDescription$Sink node)
              :topic-extraction? (not (nil? (.topicNameExtractor ^TopologyDescription$Sink node)))}))))

(defn metrics
  [streams]
  (into [] (map (fn [[_ metric]]
                  (let [metric-name (.metricName metric)]
                    {:value       (.metricValue metric)
                     :description (.description metric-name)
                     :group       (.group metric-name)
                     :name        (.name metric-name)
                     :tags        (into {} (.tags metric-name))})))
        (.metrics streams)))

(defn application-id
  [metrics]
  (some #(when (= "application-id" (:name %)) (:value %)) metrics))

(defn client-id-tag
  [metrics]
  (some #(get-in % [:tags "client-id"]) metrics))

(defn client-id
  [metrics]
  (let [app-id    (application-id metrics)
        client-id (client-id-tag metrics)]
    (some-> client-id
            (str/replace (str app-id "-") "")
            (str/split #"-StreamThread-")
            (first))))

(defn numeric-metrics
  [metrics]
  (->> metrics
       (filter (comp number? :value))
       (remove (fn [{:keys [value]}]
                 (if (double? value)
                   (Double/isNaN value)
                   false)))
       (map #(select-keys % [:name :tags :value]))))

(defn snapshot-send
  [{:keys [snapshot-topic ^Producer producer snapshot-id application-id job-id client-id captured]} data]
  (let [snapshot {:type           :kafka/streams-agent
                  :application-id application-id
                  :client-id      client-id
                  :captured       captured
                  :data           data
                  :job/id         job-id
                  :snapshot/id    snapshot-id}
        taxon    [(:domain snapshot-id) (:id snapshot-id) :kafka/streams-agent]
        record   (ProducerRecord. (:topic snapshot-topic) taxon snapshot)]
    (.get (.send producer record))))

(defn metrics-send
  [{:keys [snapshot-topic producer snapshot-id application-id job-id client-id captured]} metrics]
  (let [taxon [(:domain snapshot-id) (:id snapshot-id) :kafka/streams-agent]]
    (doseq [data (partition-all 50 metrics)]
      (let [value    {:type           :kafka/streams-agent-metrics
                      :application-id application-id
                      :client-id      client-id
                      :captured       captured
                      :data           (vec data)
                      :job/id         job-id
                      :snapshot/id    snapshot-id}
            record   (ProducerRecord. (:topic snapshot-topic) taxon value)]
        (.get (.send producer record))))
    (log/infof "kPow: sent [%s] streams metrics for application.id %s" (count metrics) application-id)))

(defn plan-send
  [{:keys [snapshot-topic producer snapshot-id job-id captured]}]
  (let [taxon    [(:domain snapshot-id) (:id snapshot-id) :kafka/streams-agent]
        plan     {:type        :observation/plan
                  :captured    captured
                  :snapshot/id snapshot-id
                  :job/id      job-id
                  :data        {:type :observe/streams-agent}}
        record   (ProducerRecord. (:topic snapshot-topic) taxon plan)]
    (.get (.send producer record))))

(defn snapshot-telemetry
  [{:keys [streams ^Topology topology] :as ctx}]
  (let [metrics (metrics streams)]
    (if (empty? metrics)
      (log/warn "KafkStreams .metrics() method returned an empty collection, no telemetry was sent. Has something mutated the global metrics registry?")
      (let [topology       (p/datafy (.describe topology))
            state          (str (.state streams))
            snapshot       {:topology topology :state state}
            client-id      (client-id metrics)
            application-id (application-id metrics)
            ctx            (assoc ctx
                                  :captured (System/currentTimeMillis)
                                  :client-id client-id
                                  :application-id application-id
                                  :snapshot-id {:domain :streams :id client-id})]
        (when (nil? application-id)
          (throw (Exception. "Cannot infer application id from metrics returned from KafkaStreams instance. Expected metric \"application-id\" in the metrics registry.")))
        (when (nil? client-id)
          (throw (Exception.
                  (format "Cannot infer client id from metrics returned from KafkaStreams instance. Got: client-id %s and application-id %s"
                          (client-id-tag metrics)
                          application-id))))
        (snapshot-send ctx snapshot)
        (metrics-send ctx (numeric-metrics metrics))
        ctx))))

(defn snapshot-task
  ^Runnable [snapshot-topic producer registered-topologies latch]
  (fn []
    (let [job-id (str (UUID/randomUUID))
          ctx    {:job-id         job-id
                  :snapshot-topic snapshot-topic
                  :producer       producer}]

      (doseq [[id [streams topology]] @registered-topologies]
        (try (when-let [next-ctx (snapshot-telemetry (assoc ctx :streams streams :topology topology))]
               (Thread/sleep 2000)
               (plan-send next-ctx))
             (catch Throwable e
               (log/errorf e "kPow: error sending streams snapshot for agent %s" id))))

      (deliver latch true))))

(defonce thread-factory
  (let [n (AtomicInteger. 0)]
    (reify ThreadFactory
      (newThread [_ r]
        (doto (Thread. r)
          (.setName (format "kpow-streams-agent-%d" (.getAndIncrement n))))))))

(defn start-registry
  [{:keys [snapshot-topic producer]}]
  (log/info "kPow: starting registry")
  (let [registered-topologies (atom {})
        pool                  (Executors/newSingleThreadScheduledExecutor thread-factory)
        register-fn           (fn [streams topology]
                                (let [id (str (UUID/randomUUID))]
                                  (swap! registered-topologies assoc id [streams topology])
                                  id))
        latch                 (promise)
        task                  (snapshot-task snapshot-topic producer registered-topologies latch)
        scheduled-future      (.scheduleWithFixedDelay pool task 500 60000 TimeUnit/MILLISECONDS)]
    {:register         register-fn
     :pool             pool
     :scheduled-future scheduled-future
     :topologies       registered-topologies
     :close            (fn [] (.shutdownNow pool))
     :latch            latch}))

(defn close-registry
  [agent]
  (log/info "kPow: closing registry")
  (when-let [close (:close agent)]
    (close))
  (when-let [registered-topologies (:topologies agent)]
    (reset! registered-topologies {}))
  {})

(defn init-registry
  [producer]
  (start-registry {:snapshot-topic kpow-snapshot-topic :producer producer}))

(defn register
  [agent streams topology]
  (when-let [register-fn (:register agent)]
    (let [id (register-fn streams topology)]
      (log/infof "kPow: registring new streams agent %s" id)
      id)))

(defn unregister
  [agent ^String id]
  (when-let [registered-topologies (:topologies agent)]
    (swap! registered-topologies dissoc id)
    (log/infof "kPow: unregistered streams agent %s" id)
    true))
