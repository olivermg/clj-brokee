(ns clj-brokee.consumer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.context :refer [*current-context*] :as ctx]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Consumer [msg-ch commit-ch])

(defn start [this]
  this)

(defn stop [this]
  this)

(defn consume-async [{:keys [msg-ch] :as this}]
  (go (let [{:keys [topic message]} (a/<! msg-ch)]
        message)))

#_(defn consume [this]
  (let [cres (consume-async this)]
    #?(:clj  (a/<!! cres)
       :cljs cres)))

(defmacro with-consumed [this msg-sym & body]
  `(go (let [message# (a/<! (consume-async ~this))]
         (binding [*current-context* (assoc *current-context*
                                            :consumed message#)]
           (let [~msg-sym (:consumed *current-context*)]
             ~@body)))))

#_(clojure.pprint/pprint
   (macroexpand-1 '(with-consumed x m
                     (println m)
                     m)))


(defn commit [this message]
  #_(a/>!! commit-ch message)  ;; is probably not safe enough, as success here only means commit msg has been delivered, not that commit was successful
  #_(throw (ex-info "not implemented" {})))
