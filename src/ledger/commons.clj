(ns ledger.commons
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn build-id [transaction] (str (get transaction "merchant") (get transaction "amount")))

(defn diff-in-minutes [to from] (t/in-minutes (t/interval (c/from-string to) (c/from-string from))))
