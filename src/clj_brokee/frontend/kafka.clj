(ns clj-brokee.frontend.kafka
  (:require [clojure.core.async :as a]
            [clj-brokee.core :as b]))

(defrecord KafkaAdapter [kafka topic-fn]

  b/Adapter

  (recv-ch [this ch]
    ;;; (k/install-listener (fn [msg] (a/put! ch msg)))
    ch)

  (send-ch [this ch]
    ;;; (a/go-loop [msg (a/<! ch)] (k/send (topic-fn msg) msg))
    ch))
