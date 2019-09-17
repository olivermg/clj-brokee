(ns clj-brokee.frontend.async
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



(defn adapter [ch]
  ch)



(comment
  (let [mb (create)
        p1 (b/producer mb)
        c1 (b/consumer mb :t1 1)]
    (future
      (let [msg (b/consume c1)]
        (println "CONSUMED" msg)
        (b/commit c1 msg)))
    (Thread/sleep 1000)
    (b/produce p1 :t1 {:x "t1 m1" :y (rand-int 1000)})
    (Thread/sleep 1000)))
