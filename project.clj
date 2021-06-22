(defproject io.operatr/streams-agent "0.1.2"
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
                   :dependencies [[org.slf4j/slf4j-api "1.7.30"]
                                  [ch.qos.logback/logback-classic "1.2.3"]
                                  [lambdaisland/kaocha "1.0.861"]]}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]}
  :java-source-paths ["src/java"]
  :source-paths ["src/clojure"])