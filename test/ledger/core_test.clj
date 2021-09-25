(ns ledger.core-test
  (:require [clojure.test :refer :all]
            [ledger.validators :refer :all]
            [ledger.core :refer :all]))

(def card-active-validator (partial check-for-violations [:card-not-active]))
(deftest test-validators
  (testing "card active validator should add violation if card is not active on previous state"
    (is (= [:card-not-active] (card-active-validator {
                                                      :event-counter 1
                                                      :account       {
                                                                      :active-card false
                                                                      }
                                                      } {})))))

(deftest account-should-be-open-when-executed
  (let [[_, updated-state] (handle-command-for-state {"account" {
                                                                      "active-card"     true
                                                                      "available-limit" 100
                                                                      }} zero)]
    (testing "account should be open when executed"
      (is (get-in updated-state [:account :active-card]))
      (is (= 100 (get-in updated-state [:account :available-limit]))))))


(deftest account-should-not-open-twice
  (let [[_, first-state] (handle-command-for-state {"account" {
                                                               "active-card"     true
                                                               "available-limit" 100
                                                               }})]
    (let [[violations, _] (handle-command-for-state {"account" {
                                                                            "active-card"     false
                                                                            "available-limit" 10
                                                                            }} first-state)]
      (testing "account should not be reopened"
        (is (= [:account-already-initialized] violations))))))
