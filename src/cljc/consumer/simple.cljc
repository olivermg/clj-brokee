(ns clj-brokee.consumer.simple
  (:require [clj-brokee.consumer :as c]))


(defrecord SimpleConsumer [subscriptions]

  c/Consumer

  (subscribe [this topics handle-fn]
    (swap! subscriptions
           #(reduce (fn [s e]
                      (update s e conj handle-fn))
                    %
                    topics))
    nil)

  #_(commit [this offset]))
