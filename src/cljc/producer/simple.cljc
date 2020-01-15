(ns clj-brokee.producer.simple
  (:require [clj-brokee.producer :as p]
            [clj-brokee.util :as u]))


(defrecord SimpleProducer [broker]

  p/Producer

  (produce [this topic message]
    (doseq [consumer (some-> broker :consumers deref)]
      (doseq [handle-fn (some-> consumer :subscriptions deref (get topic))]
        (u/run-async handle-fn topic message)))))
