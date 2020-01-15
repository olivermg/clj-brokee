(ns clj-brokee.broker)

(defprotocol Broker
  (produce [this topic message])
  (consume [this topic handle-fn]))
