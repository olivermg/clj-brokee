(ns clj-brokee.broker.kafka
  (:require [clj-brokee.core :as c]))

(defrecord KafkaBroker []

  c/Broker

  (producer [this])

  (consumer [this topic]))
