(ns clj-brokee.producer
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord Producer [ch])

(defn produce [{:keys [ch] :as this} message]
  (a/>!! ch message))
