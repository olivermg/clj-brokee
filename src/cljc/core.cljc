(ns clj-brokee.core
  (:require [clj-brokee.util :as u]))




(defprotocol Publisher
  (publish* [this data]))

(defprotocol Subscriber
  (handler [this]))


(defn publish [publisher topic message]
  (publish* publisher {:topic topic
                       :message message}))


(defn subscribe [subscriber topics handler])


(defn make-broker [])

(defn make-publisher [broker])

(defn make-subscriber [broker topics handler])
