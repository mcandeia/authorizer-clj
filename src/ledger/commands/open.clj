(ns ledger.commands.open
  (:require [ledger.validators :refer :all]))

(defn create-limit [state account] (assoc-in state [:account :available-limit] (get account "available-limit")))
(defn set-card-activation [state account] (assoc-in state [:account :active-card] (get account "active-card")))

(defn exec [command state] (let [account (get command "account")] (-> state
                                                                      (create-limit account)
                                                                      (set-card-activation account))))


(def validator (partial check-for-violations [:account-already-initialized]))