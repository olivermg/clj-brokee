(ns clj-brokee.producer.simple
  (:require [clj-brokee.producer :as p]
            [clj-brokee.util :as u]))


(defrecord SimpleProducer [broker]

  p/Producer

  (produce [this topic message]
    (doseq [consumer (get broker :consumers)]
      (doseq [handle-fn (get-in consumer [:subscriptions topic])]
        (u/run-async handle-fn topic message)))))
