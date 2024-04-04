(defproject io.operatr/kpow-streams-agent "0.2.11"
  :description "kPow's Kafka Streams monitoring agent"
  :url "https://github.com/operatr-io/kpow-streams-agent"
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
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [com.cognitect/transit-clj "1.0.333"]
                 [org.clojure/tools.logging "1.3.0"]
                 [org.apache.kafka/kafka-streams "3.2.0" :scope "provided" :exclusions [com.fasterxml.jackson.core/jackson-core]]]
  :uberjar {:prep-tasks ["clean" "javac" "compile"]
            :aot        :all}
  :classifiers [["sources" {:source-paths      ^:replace []
                            :java-source-paths ^:replace ["src/java"]
                            :resource-paths    ^:replace ["javadoc"]}]
                ["javadoc" {:source-paths      ^:replace []
                            :java-source-paths ^:replace []
                            :resource-paths    ^:replace ["javadoc"]}]]
  :profiles {:kaocha {:dependencies [[lambdaisland/kaocha "1.87.1366"]]}
             :dev    {:resource-paths ["dev-resources"]
                      :plugins        [[lein-cljfmt "0.8.0"]]
                      :dependencies   [[org.slf4j/slf4j-api "2.0.12"]
                                       [ch.qos.logback/logback-classic "1.3.14"]
                                       [cheshire "5.12.0" :exclusions [com.fasterxml.jackson.core/jackson-databind]]
                                       [clj-kondo "2024.02.12"]]}
             :smoke  {:pedantic? :abort}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
            "kondo"  ["with-profile" "+smoke" "run" "-m" "clj-kondo.main" "--lint" "src"]
            "fmt"    ["with-profile" "+smoke" "cljfmt" "check"]
            "fmtfix" ["with-profile" "+smoke" "cljfmt" "fix"]}
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :java-source-paths ["src/java"]
  :source-paths ["src/clojure"]
  :deploy-repositories [["releases" {:url   "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                                     :creds :gpg}
                         "snapshots" {:url   "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                                      :creds :gpg}]])
