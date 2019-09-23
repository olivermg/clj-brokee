(ns clj-brokee.core
  (:require [clojure.core.async :as a]))

;;; NOTE: this is just a playground for now, with mostly incomplete ideas & stuff

(comment

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




  (defn user-xf [determine-user-fn]
    )

  (defn permission-validating-xf [check-permissions-fn]
    (filter (fn [msg]
              (check-permissions-fn msg))))

  (defn dispatching-xf []
    )



  (defprotocol EventBroker
    (reg-event-db [this id handler])
    (reg-sub [this id handler])
    (dispatch [this event])))




;;;;;;;;;;;;;;;;;;;;;;;

(comment

 (defprotocol ReceivingAdapter
   (send [this message]))
 
 (defprotocol SendingAdapter
   (recv [this])
   (commit [this message]))
 
 (defrecord Bridge [adapter rx-ch tx-ch commit-ch])
 (defn construct [adapter rx-ch tx-ch commit-ch]
   (map->Bridge {:adapter   adapter
                 :rx-ch     rx-ch
                 :tx-ch     tx-ch
                 :commit-ch commit-ch}))
 (defn start [this]
   (a/go-loop [msg (a/<! rx-ch)]
     (when-not (nil? msg)
       (send adapter msg)
       (recur (a/<! rx-ch))))
   (loop [msg (recv adapter)]
     (when-not (nil? msg)
       (a/>!! tx-ch msg)
       (recur (recv adapter))))
   (a/go-loop [msg (a/<! commit-ch)]
     (when-not (nil? msg)
       (commit adapter msg)
       (recur (a/<! commit-ch))))
   this)
 (defn stop [this]
   this)
 
 
 
 (defprotocol Broker
   (producer [this])
   (consumer [this topic group-id]))
 
 #_(defprotocol Bridge
     (connect [this other]))
 
 (defprotocol Producer
   (produce [this message]))
 
 (defprotocol Consumer
   (consume [this])
   (commit [this message]))
 
 
 (defprotocol Receiver
   (tx-ch [this]))
 
 (defprotocol Sender
   (rx-ch [this])))


(comment

  (defrecord Client [backbone
                     backbone-tx-ch backbone-rx-ch])

  (defn client-foo [{:keys [backbone-tx-ch backbone-rx-ch] :as this} x]
    (a/put! backbone-tx-ch {:x x})
    (a/<!! backbone-rx-ch)))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; user/client stuff:

(defprotocol Producer
  (produce [this message]))

(defprotocol Consumer
  (consume [this])
  (commit [this message]))

(defprotocol ClientBroker
  (producer [this])
  (consumer [this topic]))

;;; backend/hub stuff:

(defprotocol BackendBroker
  (tx-ch [this])
  (rx-ch [this]))

(defrecord Hub [])
(defn connect [this adapter]
  (let [rx (rx-ch adapter)
        tx (tx-ch adapter)]
    ))
