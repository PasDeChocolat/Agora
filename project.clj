(defproject agora "0.1.0-SNAPSHOT"
  :description "The Kakaako Agora space has a framework."
  :url "https://github.com/PasDeChocolat/Agora"
  :dev-dependencies [[lein-ring "0.8.10"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [om "0.6.4"]
                 [ring "1.3.0"]
                 [compojure "1.1.8"]
                 [domina "1.0.2"]
                 [prismatic/dommy "0.1.2"]
                 [com.taoensso/timbre "3.2.1"]
                 [http-kit "2.1.18"]
                 [clj-time "0.7.0"]
                 [liberator "0.11.0"]
                 [org.clojure/data.json "0.2.5"]
                 [com.datomic/datomic-free "0.9.4815"]
                 [com.cemerick/piggieback "0.0.5"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.10"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/agora.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :pretty-print true
                                   :source-map true}}
                       {:id "prod"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/prod/agora.js"
                                   :output-dir "resources/public/js/prod/out"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :source-map "resources/public/js/prod/agora.js.map"}
                        :jar true}]}
  :main agora.server
  :ring {:handler agora.server/app})

