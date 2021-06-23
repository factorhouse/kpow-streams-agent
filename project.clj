(defproject io.operatr/kpow-streams-agent "0.1.2"
  :description "kPow streams agent"
  :url "https://github.com/operatr-io/streams-agent"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [cheshire "5.10.0"]
                 [com.cognitect/transit-clj "1.0.324"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.apache.kafka/kafka-streams "2.8.0" :scope "provided" :exclusions [com.fasterxml.jackson.core/jackson-core]]]
  :uberjar {:prep-tasks  ["clean" "javac" "compile"]
            :aot         :all
            :omit-source true}
  :profiles {:kaocha {:dependencies [[lambdaisland/kaocha "1.0.861"]]}
             :dev    {:resource-paths ["dev-resources"]
                      :plugins        [[lein-cljfmt "0.7.0"]]
                      :dependencies   [[org.slf4j/slf4j-api "1.7.31"]
                                       [ch.qos.logback/logback-classic "1.2.3"]
                                       [clj-kondo "2021.06.18"]]}
             :smoke  {:pedantic? :abort}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
            "kondo"  ["with-profile" "+smoke" "run" "-m" "clj-kondo.main" "--lint" "src"]
            "fmt"    ["with-profile" "+smoke" "cljfmt" "check"]
            "fmtfix" ["with-profile" "+smoke" "cljfmt" "fix"]}
  :java-source-paths ["src/java"]
  :source-paths ["src/clojure"])