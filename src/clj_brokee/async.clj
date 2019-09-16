(ns clj-brokee.async
  (:require [clojure.core.async :as a]
            [clj-brokee.core :as b]))


(defrecord CoreAsyncProducer [ch]

  b/Producer

  (produce* [this topic message]
    (a/put! ch {::topic topic
                ::message message})))


(defrecord CoreAsyncConsumer [ch]

  b/Consumer

  (consume [this]
    (let [{:keys [::message]} (a/<!! ch)]
      message))

  (commit [this message]
    ;;; TODO: implement
    nil))


(defrecord CoreAsyncMessageBroker [mix pub]

  b/MessageBroker

  (producer [this]
    (let [inch (a/chan)]
      (a/admix mix inch)
      (map->CoreAsyncProducer {:ch inch})))

  (consumer [this topic group-id]
    ;;; TODO: respect group-id (probably via mult)
    (let [outch (a/chan)]
      (a/sub pub topic outch)
      (map->CoreAsyncConsumer {:ch outch}))))


(defn create []
  (let [ch  (a/chan)
        mix (a/mix ch)
        pub (a/pub ch ::topic)]
    (map->CoreAsyncMessageBroker {:mix mix
                                  :pub pub})))


(comment
  (let [mb (create)
        p1 (b/producer mb)
        c1 (b/consumer mb :t1 1)]
    (future
      (println "CONSUMED" (b/consume c1)))
    (Thread/sleep 1000)
    (b/produce p1 :t1 {:x "t1 m1"})
    (Thread/sleep 1000)))
