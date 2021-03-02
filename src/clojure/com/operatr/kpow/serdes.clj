(ns com.operatr.kpow.serdes
  (:require [cognitect.transit :as transit])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)
           (org.apache.kafka.streams.kstream Windowed Window)
           (org.apache.kafka.common.serialization Serde Deserializer Serializer)))

(def windowed-writer
  (transit/write-handler
   (constantly "wd")
   (fn [^Windowed v]
     (let [k        (.key v)
           window   (.window v)
           start-ms (.start window)
           end-ms   (.end window)]
       [start-ms end-ms k]))
   (fn [v]
     (let [k        (.key v)
           window   (.window v)
           start-ms (.start window)
           end-ms   (.end window)]
       (pr-str [start-ms end-ms k])))))

(defn window [start-ms end-ms]
  (proxy [Window] [start-ms end-ms]))

(defn windowed-reader
  [[start-ms end-ms k]]
  (Windowed. k (window start-ms end-ms)))

(def write-opts
  {:handlers {Windowed windowed-writer}})

(def read-opts
  {:handlers {"wd" (transit/read-handler windowed-reader)}})

(defn transit-serialize
  [format data]
  (when data
    (let [stream (ByteArrayOutputStream.)]
      (transit/write (transit/writer stream format write-opts) data)
      (.toByteArray stream))))

(defn transit-deserialize
  [format bytes]
  (when bytes
    (transit/read (transit/reader (ByteArrayInputStream. bytes) format read-opts))))

(deftype TransitJsonSerializer []
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data] (transit-serialize :json data))
  (close [_]))

(deftype TransitJsonDeserializer []
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ bytes] (transit-deserialize :json bytes))
  (close [_]))

(deftype TransitJsonSerde []
  Serde
  (configure [_ _ _])
  (close [_])
  (serializer [_] (TransitJsonSerializer.))
  (deserializer [_] (TransitJsonDeserializer.)))

(deftype TransitJsonVerboseSerializer []
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data] (transit-serialize :json-verbose data))
  (close [_]))

(deftype TransitJsonVerboseDeserializer []
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ bytes] (transit-deserialize :json-verbose bytes))
  (close [_]))

(deftype TransitJsonVerboseSerde []
  Serde
  (configure [_ _ _])
  (close [_])
  (serializer [_] (TransitJsonSerializer.))
  (deserializer [_] (TransitJsonDeserializer.)))

(deftype TransitMsgpackSerializer []
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data] (transit-serialize :msgpack data))
  (close [_]))

(deftype TransitMsgpackDeserializer []
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ bytes] (transit-deserialize :msgpack bytes))
  (close [_]))

(deftype TransitMsgpackSerde []
  Serde
  (configure [_ _ _])
  (close [_])
  (serializer [_] (TransitMsgpackSerializer.))
  (deserializer [_] (TransitMsgpackDeserializer.)))

(defn transit-json-serializer []
  (TransitJsonSerializer.))