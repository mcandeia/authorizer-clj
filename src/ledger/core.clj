(ns ledger.core
  (:require [clojure.core.match :refer [match]]
            [ledger.commons :refer :all]
            [ledger.commands [open :as open]]
            [ledger.commands [transaction :as transaction]])
  (:gen-class))

(def zero {
                 :event-counter                       0
                 :account                             {
                                                       :active-card     false
                                                       :available-limit 0
                                                       }
                 :latest-merchant-unique-transactions {}
                 :transactions-two-minutes-window     []
                 })

(def is-open-account-command? #(contains? % "account"))
(def is-transaction-command? #(contains? % "transaction"))

(def current-state (atom zero))
(def no-op (constantly []))
(defn command-executor [command]
  (match [command]
         [(_ :guard is-open-account-command?)] [(partial open/exec command) open/validator]
         [(_ :guard is-transaction-command?)] [(partial transaction/exec command) transaction/validator]
         :else [identity no-op]))

(defn run-cmd [exec validator state]
  (let [next-state (exec (update state :event-counter inc))
        violations (validator state next-state)]
    [violations, (if (empty? violations) next-state state)]))


(defn handle-command-for-state [state command]
  (let [[exec, validator] (command-executor command)]
    (run-cmd exec validator state)))
(defn change-state-to [state] (swap! current-state (constantly state)))
(defn handle-command [command]
  (let [[violations, next-state] (handle-command-for-state @current-state command)]
    {:violations violations :account (get (change-state-to next-state) :account)}
    ))