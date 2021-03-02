(ns kpow.test-streams
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [operatr.system :as system])
  (:import (java.time Duration)
           (org.apache.kafka.streams StreamsBuilder StreamsConfig Topology KafkaStreams)
           (org.apache.kafka.streams.kstream TimeWindows ValueMapper Predicate ForeachAction ValueJoiner SessionWindows))
  (:gen-class))

(defn test-topology
  [topic]
  (let [builder (StreamsBuilder.)
        stream  (.stream builder ^String topic)
        [branch1 branch2] (-> stream
                              (.mapValues (reify ValueMapper
                                            (apply [_ value]
                                              (let [hash (str value)]
                                                {:hash  hash
                                                 :count (count hash)}))))
                              (.branch (into-array Predicate
                                                   [(reify Predicate
                                                      (test [_ k _]
                                                        (= :schema (first k))))
                                                    (reify Predicate
                                                      (test [_ k _]
                                                        (= :cluster (first k))))])))]

    (let [count-ktable1 (-> branch1
                            (.groupByKey)
                            (.count))
          count-ktable2 (-> branch2
                            (.groupByKey)
                            (.count))]

      (-> branch2
          (.leftJoin count-ktable2 (reify ValueJoiner
                                     (apply [_ v1 v2]
                                       {:v1 v1 :v2 v2})))
          (.peek (reify ForeachAction
                   (apply [_ _ v]
                     (log/debug "Branch2 applied" (pr-str v))))))

      (-> branch1
          (.join count-ktable1 (reify ValueJoiner
                                 (apply [_ v1 v2]
                                   {:v1 v1 :v2 v2})))
          (.groupByKey)
          (.windowedBy (SessionWindows/with (Duration/ofMinutes 1)))
          (.count)
          (.toStream)
          (.groupByKey)
          (.windowedBy (TimeWindows/of (Duration/ofMinutes 1)))
          (.count)
          (.toStream)
          (.peek (reify ForeachAction
                   (apply [_ _ v]
                     (log/debug "Branch2 applied" (pr-str v)))))))

    (.build builder)))

(defmethod ig/init-key :compute/test-streams
  [_ {:keys [topic config cluster agent]}]
  (let [compression (get-in topic [:config "compression.type"])]
    (let [^Topology topology    (test-topology (:topic topic))
          ^StreamsConfig config (StreamsConfig. (cond-> (merge config
                                                               (:security cluster)
                                                               (:config cluster))
                                                  (= "producer" compression) (assoc "compression.type" "gzip")))
          ^KafkaStreams streams (KafkaStreams. topology config)]
      (.cleanUp streams)
      (when-let [register-fn (:register agent)]
        (register-fn streams topology))
      (.start streams)
      streams)))

(defn test-streams []
  (ig/init (system/load-config "test-streams.edn")))

(defn -main [& _]
  (test-streams))