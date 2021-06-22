(defproject com.operatr/streams-agent "0.1.2"
  :description "kPow streams agent"
  :url "https://github.com/operatr-io/streams-agent"
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [com.cognitect/transit-clj "1.0.324"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.apache.kafka/kafka-streams "2.8.0" :scope "provided"]]
  :uberjar {:prep-tasks  ["clean" "javac" "compile"]
            :aot         :all
            :omit-source true}
  :profiles {:dev {:resource-paths ["dev-resources"]
                   :dependencies [[org.apache.kafka/kafka-streams-test-utils "2.8.0"]]}}
  :java-source-paths ["src/java"]
  :source-paths ["src/clojure"])