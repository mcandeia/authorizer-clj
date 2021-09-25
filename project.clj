(defproject ledger "0.1.0-SNAPSHOT"
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 ; datomic
                 [com.datomic/client-pro "1.0.72"]
                 ; core
                 [org.clojure/clojure "1.10.1"]
                 ; DateTime utils
                 [clj-time "0.15.2"]
                 ; Match library
                 [org.clojure/core.match "1.0.0"]
                 ; Compojure - A basic routing library
                 [compojure "1.6.1"]
                 ; Ring body parser
                 [ring/ring-json "0.5.1"]
                 ; Ring for hot reload
                 [ring "1.9.4"]
                 ; Ring defaults - for query params etc
                 [ring/ring-defaults "0.3.2"]
                 ; Clojure data.JSON library
                 [org.clojure/data.json "0.2.6"]
                 ]
  :main ^:skip-aot program
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

