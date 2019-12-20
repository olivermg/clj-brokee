(ns clj-brokee.hub.core2
  (:require [clj-brokee.util :as u]))

;;;               +--------------+
;;;               |              |
;;;               |              |
;;;        +------+    Broker    +------+
;;;        |      |              |      |
;;;        |   +->+              +<-+   |
;;;        |   |  +--------------+  |   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;; deliver|   |                    |   |deliver
;;;        |   |                    |   |
;;;        |   |emit            emit|   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;;        v   |                    |   v
;;;      +-+---+--+              +--+---+-+
;;;      |        |              |        |
;;;      | Client |              | Client |
;;;      |        |              |        |
;;;      +--+--+--+              +--+--+--+
;;;         ^  |                    |  ^
;;;         |  |                    |  |
;;;  publish|  |handle-fn  handle-fn|  |publish
;;;         +  v                    v  +
;;;         User                    User


;;; broker generic

(defn make-broker [deliver-logic]
  {:deliver-logic deliver-logic})

(defn emit [{:keys [deliver-logic clients] :as broker} client-id message]
  (dorun
   (map #(u/run-async deliver-logic % message)
        (-> (dissoc @clients client-id)
            vals))))


;;; broker implementation

(defn make-fninvocation-broker []
  (-> (make-broker (fn [deliver-fn message]
                     (deliver-fn message)))
      (assoc :clients (atom {}))))


;;; client implementation

(defn make-client [deliver-fn]
  (let [id (rand-int 2000000000)]
    {:id         id
     :deliver-fn deliver-fn
     :emit-fn    (fn [message]
                   (throw (ex-info "Client is not connected to a broker."
                                   {:client-id id
                                    :message   message})))}))

(defn publish [{:keys [emit-fn] :as client} message]
  (emit-fn message))


;;; specific client implementations

(defn make-async-client [ch]
  (make-client (fn [message]
                 (println "DELIVER VIA ASYNC" message ch))))


;;; connecting broker & client

(defn connect! [{:keys [id deliver-fn] :as client} {:keys [clients] :as broker}]
  (swap! clients assoc id deliver-fn)
  (assoc client :emit-fn
         (fn [message]
           (emit broker id message))))


;;; pubsub implementation

(defn make-pubsub [topic-fn topic deliver-fn]
  (make-client #(when (= (topic-fn %) topic)
                  (deliver-fn %))))


#_(let [b   (make-fninvocation-broker)
        c1  (-> (make-client #(println "CLIENT1" %))
                (connect! b))
        c2  (-> (make-async-client :ch2)
                (connect! b))
        c3  (-> (make-client #(println "CLIENT3" %))
                (connect! b))
        ps1 (-> (make-pubsub :id :id1 #(println "PUBSUB1" %))
                (connect! b))
        ps2 (-> (make-pubsub :id :id2 #(println "PUBSUB2" %))
                (connect! b))]
    (publish c1  {:id :id1 :x1 123})
    (publish c2  {:id :id2 :y1 234})
    (publish c3  {:id :id1 :z1 345})
    (publish ps1 {:id :id1 :x2 456})
    (publish ps1 {:id :id2 :y2 567}))
