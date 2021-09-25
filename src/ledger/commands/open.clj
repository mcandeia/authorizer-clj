(ns ledger.commands.open
  (:require [ledger.validators :refer :all]))

(defn update-from-account [prop state account] (assoc-in state [:account prop] (get account prop)))
(def update-active-card (partial update-from-account :active-card))
(def update-limit (partial update-from-account :available-limit))
(defn exec [command state]
  (let [account (get command :account)] (-> state
                                            (update-active-card account)
                                            (update-limit account))))
(def validator (partial check-for-violations [:account-already-initialized]))