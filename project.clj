(defproject sigma-chat "0.1.0-SNAPSHOT"
  :description "Sigma-chat: A secure messaging system that utilizes the Sign-and-MAC approach."
  :url "http://example.com/FIXME"
  :main sigma-chat.client.core
  :profiles {:uberjar {:main sigma-chat.client.core, :aot :all}}
  :uberjar-name "sigma.org-standalone.jar"
  :min-lein-version "2.7.1"
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/tools.logging "0.4.0"]
                 [cheshire "5.8.0"]
                 [caesium "0.10.0"]
                 [org.immutant/web "2.1.10"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-devel "1.6.3"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [buddy/buddy-sign "2.2.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [clj-time "0.14.2"]
                 [clj-http "3.7.0"]]

  :clean-targets ["static/development/js"
                  "static/release/js"
                  "static/development/index.js"
                  "static/development/index.js.map"
                  "out"
                  "target"])
