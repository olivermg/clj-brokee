(ns clj-brokee.consumer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Consumer [msg-ch commit-ch])

(defn consume [{:keys [msg-ch] :as this}]
  (-> (a/<!! msg-ch)
      (dissoc :clj-brokee/topic)))

(defn commit [this message]
  #_(a/>!! commit-ch message)  ;; is probably not safe enough, as success here only means commit msg has been delivered, not that commit was successful
  (throw (ex-info "not implemented" {})))
