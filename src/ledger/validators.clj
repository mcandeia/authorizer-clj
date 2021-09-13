(ns ledger.validators
  (:require [ledger.commons :refer :all])
  (:gen-class))

(defn is-account-created [state] (> (get state :event-counter) 0))
(defn should-have-limit [previous-state next-state]
  (or (not (is-account-created previous-state)) (and (>= (get-in next-state [:account :available-limit]) 0)
                                                     (is-account-created previous-state))))
(defn account-should-be-initialized [previous-state _] (is-account-created previous-state))

; true if the account is NOT created (which means event counter = 0)
(defn account-should-not-be-initialized [previous-state _] (not (is-account-created previous-state)))

(defn card-should-be-active [previous-state _]
  (or (not (is-account-created previous-state)) (and (is-account-created previous-state) (get-in previous-state [:account :active-card]))))
(defn check-max-frequency [max-transactions _ next-state] (<= (count (get next-state :transactions-two-minutes-window)) max-transactions))

(def high-frequency-window-size 3)
(def double-transaction-min-diff 2)
(defn check-if-window-is-acceptable [latest-occurrence-time transaction-time] (> (diff-in-minutes transaction-time latest-occurrence-time) double-transaction-min-diff))
(defn check-if-transaction-is-doubled [previous-state transaction] (let [transaction-id (build-id transaction)
                                                                         latest-occ (get-in previous-state [:latest-merchant-unique-transactions transaction-id])]
                                                                     (if (nil? latest-occ) true (check-if-window-is-acceptable latest-occ (get transaction "time")))))
(defn check-double-transaction
  [previous-state next-state]
  (let [transaction-window (get next-state :transactions-two-minutes-window)]
    (if (empty? transaction-window) true (check-if-transaction-is-doubled previous-state (first transaction-window)))))


(def violation-checkers {
                         :double-transaction            check-double-transaction
                         :high-frequency-small-interval (partial check-max-frequency high-frequency-window-size)
                         :card-not-active               card-should-be-active
                         :account-not-initialized       account-should-be-initialized
                         :insufficient-limit            should-have-limit
                         :account-already-initialized   account-should-not-be-initialized
                         })

(defn apply-validation [previous-state next-state current-violations [violation, is-valid-transition?]]
  (if (is-valid-transition? previous-state next-state) current-violations (conj current-violations violation)))

(defn check-for-violations [violations previous-state next-state]
  (reduce
    (partial apply-validation previous-state next-state)
    []
    (seq (select-keys violation-checkers violations))))