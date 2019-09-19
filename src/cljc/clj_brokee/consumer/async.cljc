(ns clj-brokee.consumer.async
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.core :as c]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

(defrecord AsyncConsumer [msg-ch commit-ch]

  c/Consumer

  (consume [this]
    (a/<!! msg-ch))

  (commit [this message]
          #_(a/>!! commit-ch message)  ;; is probably not safe enough, as success here only means commit msg has been delivered, not that commit was successful
          ))
