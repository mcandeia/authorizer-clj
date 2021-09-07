(ns ledger.core-test
  (:require [clojure.test :refer :all]
            [ledger.core :refer :all]))

(deftest testExcept
  (testing "except should filter only one number"
    (is (= [20 30] (exceptN 10 20 30)))))
