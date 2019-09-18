(ns clj-brokee.core
  (:require [clojure.core.async :as a]))

;;; NOTE: this is just a playground for now

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
  (dispatch [this event]))




;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol Receiver
  (tx-ch [this]))

(defprotocol Sender
  (rx-ch [this]))


(defrecord Backbone [xf
                     mix mult]
  ;;; TODO: use xf
  Receiver
  (tx-ch [this]
    (let [ch (a/chan)]
      (a/admix mix ch)
      ch))
  Sender
  (rx-ch [this]
    (let [ch (a/chan)]
      (a/tap mult ch)
      ch)))

(defn connect [{:keys [mix mult] :as this} other]
  (when (satisfies? Sender other)
    (a/admix mix (rx-ch other)))
  (when (satisfies? Receiver other)
    (a/tap mult (tx-ch other)))
  this)

(defn construct [this & {:keys [xf]}]  ;; xf could be filtering events by permissions etc.
  (map->Backbone {:xf xf}))

(defn start [this]
  (let [ch   (a/chan)
        mix  (a/mix ch)
        mult (a/mult ch)]
    (assoc this
           :mix  mix
           :mult mult)))


(defrecord WebsocketAdapter []
  ;;; will internally:
  ;;;  - dispatch to correct client via user-id-fn
  Sender
  (tx-ch [this])
  Receiver
  (rx-ch [this]))

(defrecord ReFrameAdapter []
  ;;; probably can't be a Sender, as we can't subscribe to all re-frame topics to forward them
  Receiver
  (rx-ch [this]))


(defrecord Client [backbone
                   backbone-tx-ch backbone-rx-ch])

(defn client-foo [{:keys [backbone-tx-ch backbone-rx-ch] :as this} x]
  (a/put! backbone-tx-ch {:x x})
  (a/<!! backbone-rx-ch))
