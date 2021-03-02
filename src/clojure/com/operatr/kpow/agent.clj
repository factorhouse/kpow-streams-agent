(ns com.operatr.kpow.agent
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [clojure.core.protocols :as p])
  (:import (org.apache.kafka.clients.producer Producer ProducerRecord)
           (org.apache.kafka.streams KafkaStreams Topology KeyValue TopologyDescription TopologyDescription$Subtopology
                                     TopologyDescription$GlobalStore TopologyDescription$Node TopologyDescription$Source
                                     TopologyDescription$Processor TopologyDescription$Sink)
           (java.util UUID)))

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
      (merge {:topic-set     (.topicSet ^TopologyDescription$Source node)
              :topic-pattern (some-> (.topicPattern ^TopologyDescription$Source node) str)})
      (instance? TopologyDescription$Processor node)
      (assoc :stores (.stores ^TopologyDescription$Processor node))
      (instance? TopologyDescription$Sink node)
      (merge {:topic             (.topic ^TopologyDescription$Sink node)
              :topic-extraction? (not (nil? (.topicNameExtractor ^TopologyDescription$Sink node)))}))))

(def kpow-snapshot-topic
  {:topic "__oprtr_snapshot_state"})

(defn metrics
  [^KafkaStreams streams]
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

(defn client-id
  [metrics]
  (let [app-id    (application-id metrics)
        client-id (some #(get-in % [:tags "client-id"]) metrics)]
    (-> (str/replace client-id (str app-id "-") "")
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
  [{:keys [snapshot-topic ^Producer producer snapshot-id application-id job-id client-id]} data]
  (let [captured (System/currentTimeMillis)
        snapshot {:type           :kafka/streams-agent
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
  [{:keys [snapshot-topic producer snapshot-id application-id job-id client-id]} metrics]
  (let [taxon [(:domain snapshot-id) (:id snapshot-id) :kafka/streams-agent]]
    (doseq [data (partition-all 250 metrics)]
      (let [captured (System/currentTimeMillis)
            value    {:type           :kafka/streams-agent-metrics
                      :application-id application-id
                      :client-id      client-id
                      :captured       captured
                      :data           (vec data)
                      :job/id         job-id
                      :snapshot/id    snapshot-id}
            record   (ProducerRecord. (:topic snapshot-topic) taxon value)]
        (.get (.send producer record))))))

(defn plan-send
  [{:keys [snapshot-topic producer snapshot-id job-id]}]
  (let [captured (System/currentTimeMillis)
        taxon    [(:domain snapshot-id) (:id snapshot-id) :kafka/streams-agent]
        plan     {:type        :observation/plan
                  :captured    captured
                  :snapshot/id snapshot-id
                  :job/id      job-id
                  :data        {:type :observe/streams-agent}}
        record   (ProducerRecord. (:topic snapshot-topic) taxon plan)]
    (.get (.send producer record))))

(defn snapshot-telemetry
  [{:keys [^KafkaStreams streams ^Topology topology] :as ctx}]
  (let [topology       (p/datafy (.describe topology))
        state          (str (.state streams))
        ;; TODO: do metrics go directly to metrics topic, or as discrete snapshot events?
        metrics        (metrics streams)
        snapshot       {:topology topology :state state}
        client-id      (client-id metrics)
        application-id (application-id metrics)
        ctx            (assoc ctx
                              :client-id client-id
                              :application-id application-id
                              :snapshot-id {:domain :streams :id client-id})]
    (snapshot-send ctx snapshot)
    (metrics-send ctx (numeric-metrics metrics))
    ctx))

(defn snapshot-loop
  [registered-topologies _close-ch snapshot-topic producer]
  (async/go-loop []
    (let [job-id (str (UUID/randomUUID))
          ctx    {:job-id         job-id
                  :snapshot-topic snapshot-topic
                  :producer       producer}]

      (doseq [[_ [streams topology]] @registered-topologies]
        (try (let [next-ctx (snapshot-telemetry (assoc ctx :streams streams :topology topology))]
               (async/<! (async/timeout 2000))
               (plan-send next-ctx))
             (catch Throwable e
               (log/error e "kPow: error sending streams snapshot")))))

    (async/<! (async/timeout 60000))
    (recur)))

(defn start-agent
  [{:keys [snapshot-topic producer]}]
  (log/info "kPow: starting agent")
  (let [registered-topologies (atom {})
        close-ch              (async/chan)
        register-fn           (fn [streams topology]
                                (let [id (str (UUID/randomUUID))]
                                  (swap! registered-topologies assoc id [streams topology])
                                  id))]
    {:register      register-fn
     :close-ch      close-ch
     :topologies    registered-topologies
     :snapshot-loop (snapshot-loop registered-topologies close-ch snapshot-topic producer)}))

(defn close-agent
  [agent]
  (log/info "kPow: closing agent")
  (when-let [close-ch (:close-ch agent)]
    (async/close! close-ch))
  (when-let [snapshot-loop (:snapshot-loop agent)]
    (async/close! snapshot-loop))
  (when-let [registered-topologies (:topologies agent)]
    (reset! registered-topologies {}))
  {})

(defn init-agent
  [producer]
  (start-agent {:snapshot-topic kpow-snapshot-topic :producer producer}))

(defn register
  [agent streams topology]
  (when-let [register-fn (:register agent)]
    (let [id (register-fn streams topology)]
      (log/infof "kPow: registring new KafkaStreams instance %s" id)
      id)))

(defn unregister
  [agent ^String id]
  (when-let [registered-topologies (:topologies agent)]
    (swap! registered-topologies dissoc id)
    (log/infof "kPow: unregistered KafkaStreams instance %s" id)
    true))

