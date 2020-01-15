(ns clj-brokee.broker.simple
  (:require [clj-brokee.broker :as b]
            [clj-brokee.util :as u]))


(defrecord SimpleBroker [topic-consumer-map]

  b/Broker

  (produce [this topic message]
    (doseq [handle-fn (get topic-consumer-map topic)]
      (u/run-async handle-fn topic message)))

  (consume [this topic handle-fn]
    (swap! topic-consumer-map #(update % topic conj handle-fn))
    this))


(defn make-simple-broker []
  (map->SimpleBroker {:topic-consumer-map (atom {})}))
