(ns clj-brokee.producer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.context :refer [*current-context*] :as ctx]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Producer [ch])

(defn start [this]
  this)

(defn stop [this]
  this)

(defn produce-async [{:keys [ch] :as this} topic message]
  (go (let [message (with-meta message
                      *current-context*)
            chmsg   {:topic   topic
                     :message message}]
        (a/>! ch chmsg)
        nil)))

(defmacro with-produced [this topic message & body]
  `(go (a/<! (produce-async ~this ~topic ~message))
       ~@body))
