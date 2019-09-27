(ns clj-brokee.broker.kafka
  (:require [clj-brokee.core :as c]))

(defrecord KafkaBroker []

  c/ClientBroker

  (producer [this])

  (consumer [this topic]))
