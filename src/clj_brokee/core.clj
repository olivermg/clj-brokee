(ns clj-brokee.core
  (:require [clojure.core.async :as a]))

(defprotocol Broker
  (producer-channel [this])
  (consumer-channel [this topic group-id])
  (commit-channel [this]))

(defprotocol Producer
  (produce* [this topic message]))

(defprotocol Consumer
  (consume [this])
  (commit [this message]))

(defprotocol RawConsumer
  (consume [this]))


(defn produce [this topic message]
  (produce* this topic message)
  nil)



(defprotocol Adapter
  (recv-ch [this ch])
  (send-ch [this ch]))
