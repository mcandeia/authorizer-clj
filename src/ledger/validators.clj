(ns ledger.validators
  (:require [ledger.commons :refer :all]
            [clojure.core.match :refer [match]])
  (:gen-class))
(def OK true)
(def VIOLATES false)

(defn is-account-created [state] (> (get state :event-counter) 0))
(defn has-balance [state] (>= (get-in state [:account :available-limit]) 0))
(defn is-card-active [state] (get-in state [:account :active-card]))

(defn should-have-limit [previous-state next-state]
  (match [previous-state next-state]
         [(_ :guard (complement is-account-created)) _] OK
         [(_ :guard is-account-created) (_ :guard has-balance)] OK
         :else VIOLATES))

(defn account-should-be-initialized [previous-state _] (is-account-created previous-state))

; true if the account is NOT created (which means event counter = 0)
(defn account-should-not-be-initialized [previous-state _] (not (is-account-created previous-state)))

(defn card-should-be-active [previous-state _]
  (match [previous-state]
         [(_ :guard (complement is-account-created))] OK
         [(_ :guard #(and (is-card-active %) (is-account-created %)))] OK
         :else VIOLATES))

(defn check-max-frequency [max-transactions _ next-state] (<= (count (get next-state :transactions-two-minutes-window)) max-transactions))

(def high-frequency-window-size 3)
(def double-transaction-min-diff 2)
(defn check-if-window-is-acceptable [latest-occurrence-time transaction-time] (> (diff-in-minutes transaction-time latest-occurrence-time) double-transaction-min-diff))
(defn check-if-transaction-is-doubled [previous-state transaction] (let [transaction-id (build-id transaction)
                                                                         latest-occ (get-in previous-state [:latest-merchant-unique-transactions transaction-id])]
                                                                     (if (nil? latest-occ) true (check-if-window-is-acceptable latest-occ (get transaction :time)))))
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