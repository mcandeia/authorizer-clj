(ns ledger.commands.transaction
  (:require [ledger.commons :refer :all]
            [ledger.validators :refer :all]))


(def high-frequency-limit-in-minutes 2)
(defn is-high-frequency [from transaction] (< (diff-in-minutes from (get transaction "time")) high-frequency-limit-in-minutes))
(defn slide [compare-from transactions] (filter (partial is-high-frequency compare-from) transactions))
(defn update-limit [state transaction] (update-in state [:account :available-limit] - (get transaction "amount")))
(defn add-unique-transaction [state transaction] (update state :latest-merchant-unique-transactions #(assoc % (build-id transaction) (get transaction "time"))))
(defn add-two-minute-window-transaction [state transaction] (update state :transactions-two-minutes-window #(conj (slide (get transaction "time") %) transaction)))

(defn exec [command state] (let [transaction (get command "transaction")] (-> state
                                                                              (update-limit transaction)
                                                                              (add-unique-transaction transaction)
                                                                              (add-two-minute-window-transaction transaction))))

(def validator (partial check-for-violations [:account-not-initialized
                                              :card-not-active
                                              :insufficient-limit
                                              :high-frequency-small-interval
                                              :double-transaction]))
