(ns ledger.persistence.database
  (:require [datomic.client.api :as d])
  (:gen-class))
(def db-name "ledger")
(def cfg {:server-type        :peer-server
          :access-key         "admin"
          :secret             "datomic"
          :endpoint           "localhost:8998"
          :validate-hostnames false})

(def client (d/client cfg))

(def state-schema [{:db/ident       :state/event-counter
                    :db/valueType   :db.type/long
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The number of modifications made in such account"}

                   {:db/id                 #db/id[:db.part/db]
                    :db/ident              :state/account
                    :db/isComponent        true
                    :db/valueType          :db.type/ref
                    :db/cardinality        :db.cardinality/one
                    :db.install/_attribute :db.part/db
                    :db/doc                "The account state"}

                   {:db/ident       :account/active-card
                    :db/valueType   :db.type/boolean
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The card activation"}

                   {:db/ident       :account/available-limit
                    :db/valueType   :db.type/bigdec
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The account current limit"}

                   {:db/id                 #db/id[:db.part/db]
                    :db/ident              :state/transactions
                    :db/isComponent        true
                    :db/valueType          :db.type/ref
                    :db/cardinality        :db.cardinality/many
                    :db.install/_attribute :db.part/db
                    :db/doc                "The map of merchant transactions"}

                   {:db/ident       :transactions/amount
                    :db/valueType   :db.type/bigdec
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The transaction amount"}

                   {:db/ident       :transactions/merchant
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The transaction merchant"}

                   {:db/ident       :transactions/time
                    :db/valueType   :db.type/instant
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The transaction instant"}])

(def conn (d/connect client {:db-name db-name}))

(d/transact conn {:tx-data state-schema})

(def account-q '[:find ?counter ?account ?transactions
                 :where [?e :state/event-counter ?counter]
                 [?e :state/account ?account]
                 [?e :state/transactions ?transactions]])

(defn from-transaction [transaction] ({
                                       :transactions/amount   (get transaction :amount)
                                       :transactions/merchant (get transaction :merchant)
                                       :transactions/time     (get transaction :time)
                                       }))
(defn from-account [account] ({
                               :account/active-card     (get account :active-card)
                               :account/available-limit (get account :available-limit)
                               }))
(defn from-state [state] ([{
                            :state/event-counter (get state :event-counter)
                            :state/account       (from-account (get state :account))
                            :state/transactions  (map from-transaction (get state :transactions-two-minutes-window))
                            }]))

(defn to-state [query-result] ({}))
(defn commit [state]
  (d/transact conn {:tx-data (from-state state)})
  state)

(defn latest [] (to-state (d/q account-q ((d/db conn)))))