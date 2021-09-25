(ns ledger.core
  (:require [clojure.core.match :refer [match]]
            [ledger.commons :refer :all]
            [ledger.commands [allowlist :as allowlist]]
            [ledger.commands [open :as open]]
            [ledger.commands [transaction :as transaction]])
  (:gen-class))

(def zero {
           :event-counter                       0
           :account                             {
                                                 :allow-listed    false
                                                 :active-card     false
                                                 :available-limit 0
                                                 }
           :latest-merchant-unique-transactions {}
           :transactions-two-minutes-window     []
           })

(def current-state (atom zero))
(def no-op (constantly []))
(defn command-executor [command]
  (match [command]
         [({:allow-list _} :only [:allow-list])] [(partial allowlist/exec command) allowlist/validator]
         [({:account _} :only [:account])] [(partial open/exec command) open/validator]
         [({:transaction _} :only [:transaction])] [(partial transaction/exec command) transaction/validator]
         :else [identity no-op]))

(defn run-cmd [exec validator state]
  (let [next-state (exec (update state :event-counter inc))
        violations (validator state next-state)]
    [violations, (if (empty? violations) next-state state)]))


(defn handle-command-for-state
  ([command] (handle-command-for-state command zero))
  ([command state]
   (let [[exec, validator] (command-executor command)]
     (run-cmd exec validator state))))

(defn change-state-to [state] (swap! current-state (constantly state)))

(defn handle-command [command]
  (let [[violations, next-state] (handle-command-for-state command @current-state)]
    {:violations violations :account (get (change-state-to next-state) :account)}
    ))