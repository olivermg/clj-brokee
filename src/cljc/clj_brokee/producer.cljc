(ns clj-brokee.producer)

(defprotocol Producer
  (produce [this topic message]))
