(ns api.routes
  (:require [ledger.core :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.defaults :refer :all]
            [clojure.data.json :as json])
  (:gen-class))

(defn as-json [resp] (str (json/write-str resp)))

(defn execute-command [cmd-json]
  (-> cmd-json
      (handle-command)
      (as-json)))

(defn execute-command-req [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (-> (get req :body)
                (execute-command))})

(defroutes ledger-routes
           (POST "/commands" [] (wrap-json-body execute-command-req))
           (route/not-found "Error, page not found!"))