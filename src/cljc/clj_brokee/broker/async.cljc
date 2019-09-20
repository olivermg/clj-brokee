(ns clj-brokee.broker.async
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.core :as c]
            [clj-brokee.producer.async :as pa]
            [clj-brokee.consumer.async :as ca]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord AsyncBroker [topic-fn
                        ch mix pub]

  c/ClientBroker

  (producer [{:keys [mix] :as this}]
    (let [ch (a/chan)]
      (a/admix mix ch)
      (pa/map->AsyncProducer {:ch ch})))

  (consumer [{:keys [pub] :as this} topic]
    (let [ch (a/chan)]
      (a/sub pub topic ch)
      (pa/map->AsyncConsumer {:ch ch})))
  
  c/BackendBroker
  
  (rx-ch [this])
  
  (tx-ch [this]))

(defn construct [topic-fn]
  (map->AsyncBroker {:topic-fn topic-fn}))

(defn start [{:keys [topic-fn] :as this}]
  (let [ch  (a/chan)
        mix (a/mix ch)
        pub (a/pub ch topic-fn)]
    (assoc this
           :mix mix
           :pub pub)))
