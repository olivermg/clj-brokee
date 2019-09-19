(ns clj-brokee.broker.async
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.core :as c]
            [clj-brokee.producer.async :as pa]
            [clj-brokee.consumer.async :as ca]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord AsyncBroker [incoming-ch outgoing-ch incoming-mix outgoing-mult
                        mix mult]

  c/ClientBroker

  (producer [{:keys [mix] :as this}]
    (let [ch (a/chan)]
      (a/admix mix ch)
      (pa/map->AsyncProducer {:ch ch})))

  (consumer [{:keys [mult] :as this} topic]
    (let [ch (a/chan)]
      (a/tap mult ch)
      (pa/map->AsyncConsumer {:ch ch})))
  
  c/BackendBroker
  
  (rx-ch [this])
  
  (tx-ch [this]))

(defn construct [incoming-ch outgoing-ch]
  (map->AsyncBroker {:incoming-ch incoming-ch
                     :outgoing-ch outgoing-ch}))

(defn start [{:keys [incoming-ch outgoing-ch] :as this}]
  (let [mix  (a/mix outgoing-ch)
        mult (a/mult incoming-ch)]
    (assoc this
           :mix  mix
           :mult mult)))
