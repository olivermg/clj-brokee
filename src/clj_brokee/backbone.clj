(ns clj-brokee.backbone)

(defprotocol Backbone
  (connect [this broker])
  (dispatch [this message]))

(defprotocol Broker
  (deliver [this message])
  (register-backbone [this backbone]))
