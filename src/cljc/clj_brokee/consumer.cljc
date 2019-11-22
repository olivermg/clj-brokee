(ns clj-brokee.consumer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Consumer [msg-ch commit-ch])

(defn consume-async [{:keys [msg-ch] :as this}]
  (go (let [{:keys [topic message]} (a/<! msg-ch)]
        message)))

(defn consume [this]
  (let [cres (consume-async this)]
    #?(:clj  (a/<!! cres)
       :cljs cres)))

(defn commit [this message]
  #_(a/>!! commit-ch message)  ;; is probably not safe enough, as success here only means commit msg has been delivered, not that commit was successful
  #_(throw (ex-info "not implemented" {})))
