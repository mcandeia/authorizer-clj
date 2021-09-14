(ns ledger.commands.open
  (:require [ledger.validators :refer :all]))

(defn exec [command state] (assoc state :account (get command :account)))
(def validator (partial check-for-violations [:account-already-initialized]))