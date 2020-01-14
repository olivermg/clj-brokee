(ns clj-brokee.producer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.context :refer [*current-context*] :as ctx]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Producer [ch])

(defmacro produce
  "Produces message. Must be used within the context of a
go block (either explicitly or e.g. within the body of with-consumed)."
  [this message]
  `(do (when-not (map? ~message)
         (throw (ex-info "produced message must be a map" {:message ~message})))
       (let [message# (with-meta ~message
                        *current-context*)]
         (a/>! (:ch ~this) message#))))

#_(macroexpand-1 '(produce {:ch (a/chan)} {:foo :bar}))

(defn produce-async
  "Asynchronously produces message."
  [this message]
  (go (produce this message)))

(defmacro with-produced
  "Evaluates body after producing a message."
  [this message & body]
  `(go (a/<! (produce ~this ~message))
       ~@body))
