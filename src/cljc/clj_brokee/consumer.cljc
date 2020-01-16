(ns clj-brokee.consumer)

(defprotocol Consumer
  (subscribe [this topics handle-fn])
  #_(commit [this offset]))
