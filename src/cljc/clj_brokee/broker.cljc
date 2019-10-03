(ns clj-brokee.broker
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.bridge :as b]
            [clj-brokee.producer :as p]
            [clj-brokee.consumer :as c]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))


;;;                 bridges                      bridges
;;;                 ^      ^                     +     +
;;;                 |      |                     |     |
;;;                 |      |                     |     |
;;;                 |      |                     v     v
;;;             +---+------+---+             +---+-----+---+
;;;             |              |             |             |
;;;       +---->+  bridge-mult +------------>+  bridge-mix +----+
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


(defrecord Broker [topic-fn
                   client-mix cbch bridge-mult bbch bridge-mix bcch client-pub]

  b/Bridge

  (tx-ch [{:keys [bridge-mix] :as this}]
    (let [ch (a/chan)]
      (a/admix bridge-mix ch)
      ch))

  (rx-ch [{:keys [bridge-mult] :as this}]
    (let [ch (a/chan)]
      (a/tap bridge-mult ch)
      ch)))

(defn construct [topic-fn]
  (map->Broker {:topic-fn topic-fn}))

(defn start [{:keys [topic-fn] :as this}]
  (let [cbch         (a/chan)
        bbch         (a/chan)
        bcch         (a/chan)
        client-mix   (a/mix cbch)
        bridge-mult  (a/mult cbch)
        bridge-mix   (a/mix bcch)
        client-pub   (a/pub bcch topic-fn)]
    (a/tap bridge-mult bbch)
    (a/admix bridge-mix bbch)
    (assoc this
           :client-mix   client-mix
           :cbch         cbch
           :bridge-mult  bridge-mult
           :bbch         bbch
           :bridge-mix   bridge-mix
           :bcch         bcch
           :client-pub   client-pub)))

(defn stop [{:keys [cbch bbch bcch] :as this}]
  (a/close! cbch)
  (a/close! bbch)
  (a/close! bcch)
  (assoc this
         :client-mix   nil
         :cbch         nil
         :bridge-mult  nil
         :bbch         nil
         :bridge-mix   nil
         :bcch         nil
         :client-pub   nil))

(defn producer [{:keys [client-mix] :as this}]
  (let [ch (a/chan)]
    (a/admix client-mix ch)
    (p/map->Producer {:ch ch})))

(defn consumer [{:keys [client-pub] :as this} topic]
  (let [ch (a/chan)]
    (a/sub client-pub topic ch)
    (c/map->Consumer {:msg-ch ch})))
