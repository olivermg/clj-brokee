(ns clj-brokee.consumer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.context :refer [*current-context*] :as ctx]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Consumer [msg-ch commit-ch])

(defmacro consume
  "Consumes message asynchronously. Must be used within the context
of a go block."
  [this]
  `(a/<! (:msg-ch ~this)))

#_(macroexpand-1 '(consume {:msg-ch (a/chan)}))

(defn consume-async
  "Asynchronously consumes message. Returns a channel that can be
used to retrieve consumed message."
  [this]
  (go (consume this)))

(defmacro with-consumed
  "Evaluates body after consuming a message. The consumed 
message is bound to symbol msg-sym throughout the scope of body.

Also sets up a dynamic context for when you desire to produce
messages as 'responses' to the consumed message. The messages produced
from within body will thus be tagged with the information that they've
been produced within the context of the consumed message.

So a common pattern for responding to a consumed message is:

(c/with-consumed consumer msg
  (p/produce producer
             {:x (some-> msg :x inc)}))"
  [this msg-sym & body]
  `(go (let [~msg-sym (consume ~this)]
         (binding [*current-context* (assoc *current-context*
                                            :consumed ~msg-sym)]
           ~@body))))

#_(macroexpand-1 '(with-consumed this m
                    (println m)
                    m))


(defmacro commit
  "Commits a message asynchronously. Must be used within the body
of with-consumed."
  [this message]
  ;;; TODO: will probably not be sufficient yet:
  `(a/>! (:commit-ch ~this) ~message))
