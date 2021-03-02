(defproject wavejumper/streams-agent "0.1.2"
  :description "kPow streams agent"
  :url "https://github.com/operatr-io/streams-agent"
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/core.async "1.3.610"]
                 [com.cognitect/transit-clj "1.0.324"]
                 [org.apache.kafka/kafka-streams "2.6.0" :scope "provided"]]
  :uberjar {:prep-tasks  ["clean" "javac" "compile"]
            :aot         :all
            :omit-source true}
  :profiles {:dev {:resource-paths ["dev-resources"]}}
  :java-source-paths ["src/java"]
  :source-paths ["src/clojure"])
