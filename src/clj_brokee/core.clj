(ns clj-brokee.core
  (:require [clojure.core.async :as a]))

(defprotocol MessageBroker
  (get-producer [this topic])
  (subscribe [this topic group-id]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn -main [& args]
  (println "aaaaaaaaaaa")
  (foo 123))
