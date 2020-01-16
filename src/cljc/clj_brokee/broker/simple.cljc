(ns clj-brokee.broker.simple
  (:require [clj-brokee.broker :as b]
            [clj-brokee.consumer.simple :as c]
            [clj-brokee.producer.simple :as p]))


(defrecord SimpleBroker [consumers]

  b/Broker

  (producer [this]
    (p/map->SimpleProducer {:broker this}))

  (consumer [this]
    (let [consumer (c/map->SimpleConsumer {:subscriptions (atom {})})]
      (swap! consumers #(conj % consumer))
      consumer)))


(defn make-simple-broker []
  (map->SimpleBroker {:consumers (atom [])}))



#_(let [b  (make-simple-broker)
        c1 (b/consumer b)
        c2 (b/consumer b)
        p1 (b/producer b)
        p2 (b/producer b)]
    (clj-brokee.consumer/subscribe c1 #{:t1} #(println "T1" %1 %2))
    (clj-brokee.consumer/subscribe c2 #{:t2 :t3} #(println "T2/3" %1 %2))
    (clj-brokee.producer/produce p1 :t1 {:foo "bar1"})
    (Thread/sleep 100)
    (clj-brokee.producer/produce p1 :t2 {:foo "bar2"})
    (Thread/sleep 100)
    (clj-brokee.producer/produce p2 :t3 {:foo "bar3"}))
