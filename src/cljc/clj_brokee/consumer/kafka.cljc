(ns clj-brokee.consumer.kafka
  (:require [clj-brokee.core :as c]))

(defrecord KafkaConsumer []
  
  c/Consumer
  
  (consume [this])
  
  (commit [this message]))
