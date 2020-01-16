(ns clj-brokee.broker)

(defprotocol Broker
  (producer [this])
  (consumer [this]))
