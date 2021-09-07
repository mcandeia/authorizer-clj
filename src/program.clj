(ns program
  (:require [api.routes :refer :all] )
  (:require [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer :all])
  (:gen-class))

(def port 3000)
(def with-anti-forgery (-> site-defaults
                           (assoc-in [:security :anti-forgery] false)))
(defn app
  []
  (-> ledger-routes
      (wrap-reload)
      (wrap-defaults with-anti-forgery)))

(defn start-server
  []
  (jetty/run-jetty (app) {:port port :join? false}))

(defn -main [& args]
  (start-server)
  (println (str "Running webserver at http://127.0.0.1:" port "/")))
