(defproject io.operatr/kpow-streams-agent "0.2.2"
  :description "kPow's Kafka Streams monitoring agent"
  :url "https://github.com/operatr-io/streams-agent"
  :license {:name         "Apache-2.0 License"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo
            :comments     "same as Kafka"}
  :scm {:name "git" :url "https://github.com/operatr-io/kpow-streams-agent"}
  :pom-addition ([:developers
                  [:developer
                   [:id "wavejumper"]
                   [:name "Thomas Crowley"]
                   [:url "https://operatr.io"]
                   [:roles
                    [:role "developer"]
                    [:role "maintainer"]]]
                  [:developer
                   [:id "d-t-w"]
                   [:name "Derek Troy-West"]
                   [:url "https://operatr.io"]
                   [:roles
                    [:role "developer"]
                    [:role "maintainer"]]]])
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
  :source-paths ["src/clojure"]
  :deploy-repositories [["releases" {:url   "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                                     :creds :gpg}
                         "snapshots" {:url   "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                                      :creds :gpg}]])