(ns clj-brokee.broker.async
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.core :as c]
            [clj-brokee.producer.async :as pa]
            [clj-brokee.consumer.async :as ca]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))


;;;                 backends                     backends
;;;                 ^      ^                     +     +
;;;                 |      |                     |     |
;;;                 |      |                     |     |
;;;                 |      |                     v     v
;;;             +---+------+---+             +---+-----+---+
;;;             |              |             |             |
;;;       +---->+ backend-mult +------------>+ backend-mix +----+
;;;       |     |              |    bbch     |             |    |
;;;       |     +--------------+             +-------------+    |
;;;       |                                                     |
;;;       |                                                     |bcch
;;;       |cbch                                                 |
;;;       |                                                     |
;;;       |                                                     v
;;; +-----+------+                                      +-------+----+
;;; |            |                                      |            |
;;; | client-mix |                                      | client-pub |
;;; |            |                                      |            |
;;; +-+----+---+-+                                      +--+---+---+-+
;;;   ^    ^   ^                                           |   |   |
;;;   |    |   |                                           |   |   |
;;;   +    +   +                                           v   v   v
;;;    producers                                           consumers


(defrecord AsyncBroker [topic-fn
                        client-mix cbch backend-mult bbch backend-mix bcch client-pub]

  c/ClientBroker

  (producer [{:keys [client-mix] :as this}]
    (let [ch (a/chan)]
      (a/admix client-mix ch)
      (pa/map->AsyncProducer {:ch ch})))

  (consumer [{:keys [client-pub] :as this} topic]
    (let [ch (a/chan)]
      (a/sub client-pub topic ch)
      (ca/map->AsyncConsumer {:msg-ch ch})))

  c/BackendBroker

  (tx-ch [{:keys [backend-mix] :as this}]
    (let [ch (a/chan)]
      (a/admix backend-mix ch)
      ch))

  (rx-ch [{:keys [backend-mult] :as this}]
    (let [ch (a/chan)]
      (a/tap backend-mult ch)
      ch)))

(defn construct [topic-fn]
  (map->AsyncBroker {:topic-fn topic-fn}))

(defn start [{:keys [topic-fn] :as this}]
  (let [cbch         (a/chan)
        bbch         (a/chan)
        bcch         (a/chan)
        client-mix   (a/mix cbch)
        backend-mult (a/mult cbch)
        backend-mix  (a/mix bcch)
        client-pub   (a/pub bcch topic-fn)]
    (a/tap backend-mult bbch)
    (a/admix backend-mix bbch)
    (assoc this
           :client-mix   client-mix
           :cbch         cbch
           :backend-mult backend-mult
           :bbch         bbch
           :backend-mix  backend-mix
           :bcch         bcch
           :client-pub   client-pub)))

(defn stop [{:keys [cbch bbch bcch] :as this}]
  (a/close! cbch)
  (a/close! bbch)
  (a/close! bcch)
  (assoc this
         :client-mix   nil
         :cbch         nil
         :backend-mult nil
         :bbch         nil
         :backend-mix  nil
         :bcch         nil
         :client-pub   nil))
