(ns clj-brokee.broker.simple
  (:require [clj-brokee.broker :as b]
            [clj-brokee.consumer.simple :as c]
            [clj-brokee.producer.simple :as p]))


(defrecord SimpleBroker [consumers]

  b/Broker

  (producer [this]
    (p/map->SimpleProducer this))

  (consumer [this]
    (let [consumer (c/map->SimpleConsumer {:subscriptions (atom {})})]
      (swap! consumers #(conj % consumer))
      consumer)))


(defn make-simple-broker []
  (map->SimpleBroker {:consumers (atom [])}))
