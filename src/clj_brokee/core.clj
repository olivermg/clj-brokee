(ns clj-brokee.core
  (:require [clojure.core.async :as a]))

(defprotocol MessageBroker
  (get-producer [this topic])
  (subscribe [this topic group-id]))

(defrecord CoreAsyncMessageBroker []

  MessageBroker

  (get-producer [this topic]
    (a/chan))

  (subscribe [this topic group-id]
    (a/chan)))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn -main [& args]
  (println "aaaaaaaaaaa")
  (foo 123))