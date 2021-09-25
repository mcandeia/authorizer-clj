(ns program
  (:require [api.routes :refer :all]
            [clojure.data.json :as json])
  (:require [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer :all])
  (:gen-class)
  (:import (java.io BufferedReader)))

(def port 3000)
(def with-anti-forgery (-> site-defaults
                           (assoc-in [:security :anti-forgery] false)))
(defn app
  []
  (-> ledger-routes
      (wrap-json-body {:key-fn keyword})
      (wrap-reload)
      (wrap-defaults with-anti-forgery)))

(defn start-server
  []
  (jetty/run-jetty (app) {:port port :join? false}))

(defn -main2 [& args]
  (start-server)
  (println (str "Running webserver at http://127.0.0.1:" port "/")))

(defn -main [& args]
  (doseq [ln (line-seq (BufferedReader. *in*))]
    (println (execute-command (json/read-str ln :key-fn keyword)))))

