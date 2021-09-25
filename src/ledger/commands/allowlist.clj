(ns ledger.commands.allowlist
  (:require [ledger.validators :refer :all])),


(defn exec [command state]
  (let [activation (get-in command [:allow-list :active])]
    (assoc-in state [:account :allow-listed] activation)))

(def validator (partial check-for-violations []))
