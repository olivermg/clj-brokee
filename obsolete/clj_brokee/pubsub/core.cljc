(ns clj-brokee.pubsub.core)

(defprotocol Pubsub
  (emit [this message])
  (subscribe [this topic]))
