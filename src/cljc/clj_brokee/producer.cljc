(ns clj-brokee.producer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.context :refer [*current-context*] :as ctx]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Producer [ch])

(defmacro produce
  "Produces message under topic. Must be used within the context of a
go block (either explicitly or e.g. within the body of with-consumed)."
  [this topic message]
  `(let [message# (with-meta ~message
                    *current-context*)
         chmsg#   {:topic   ~topic
                   :message message#}]
     (a/>! (:ch ~this) chmsg#)))

#_(macroexpand-1 '(produce {:ch (a/chan)} :topic1 {:foo :bar}))

(defn produce-async
  "Asynchronously produces message under topic."
  [this topic message]
  (go (produce this topic message)))

(defmacro with-produced
  "Evaluates body after producing a message."
  [this topic message & body]
  `(go (a/<! (produce ~this ~topic ~message))
       ~@body))
