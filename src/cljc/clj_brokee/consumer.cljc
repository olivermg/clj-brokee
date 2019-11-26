(ns clj-brokee.consumer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.context :refer [*current-context*] :as ctx]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Consumer [msg-ch commit-ch])

(defn consume
  "Consumes message asynchronously. Must be used within the context
of a go block."
  [{:keys [msg-ch] :as this}]
  (let [{:keys [topic message]} (a/<! msg-ch)]
    message))

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
  (p/produce producer :response-topic
             {:x (some-> msg :x inc)}))

assuming that the consumer on the other end is expecting answers to
be produced under the topic :response-topic."
  [this msg-sym & body]
  `(go (let [~msg-sym (a/<! (consume ~this))]
         (binding [*current-context* (assoc *current-context*
                                            :consumed ~msg-sym)]
           ~@body))))

#_(clojure.pprint/pprint
   (macroexpand-1 '(with-consumed x m
                     (println m)
                     m)))


(defn commit
  "Commits a message asynchronously. Must be used within the body
of with-consumed."
  [{:keys [commit-ch] :as this} message]
  ;;; TODO: will probably not be sufficient yet:
  (a/>! commit-ch message))
