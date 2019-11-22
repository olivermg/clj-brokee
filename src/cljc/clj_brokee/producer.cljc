(ns clj-brokee.producer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Producer [ch])

(defn produce-async [{:keys [ch] :as this} topic message]
  (go (let [chmsg {:topic   topic
                   :message message}]
        (a/>! ch chmsg)
        nil)))

(defn produce [this topic message]
  (let [pres (produce-async this topic message)]
    #?(:clj  (a/<!! pres)
       :cljs pres)))
