(ns clj-brokee.async
  (:require [clojure.core.async :as a]
            [clj-brokee.core :as b]))

(defrecord CoreAsyncMessageBroker [ch]

  b/MessageBroker

  (get-producer [this topic]
    (let [pch (a/chan)]
      (a/pipe ch pch))

  (subscribe [this topic group-id]
    (a/chan)))

(defn create []
  (map->CoreAsyncMessageBroker {:ch (a/chan)}))
