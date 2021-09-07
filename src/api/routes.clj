(ns api.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.defaults :refer :all]
            [clojure.data.json :as json])
  (:gen-class))

(defn get-parameter [req p-name] (get (:params req) p-name))

(defn hello-world [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World !"})

(defn from-body [req property]
  (get-in req [:body property]))
(defn echo-request [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (-> (let [take-prop (partial from-body req)]
                (str (json/write-str {:name (take-prop "name")}))))})


(defroutes ledger-routes
           (GET "/" [] hello-world)
           (POST "/echo" [] (wrap-json-body echo-request))
           (route/not-found "Error, page not found!"))