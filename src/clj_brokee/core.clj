(ns clj-brokee.core)

(defprotocol MessageBroker
  (producer [this])
  (consumer [this topic group-id]))

(defprotocol Producer
  (produce* [this topic message]))

(defprotocol Consumer
  (consume [this])
  (commit [this message]))


(defn produce [this topic message]
  (produce* this topic message)
  nil)