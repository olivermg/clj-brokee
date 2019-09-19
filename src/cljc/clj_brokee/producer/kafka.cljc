(ns clj-brokee.producer.kafka
  (:require [clj-brokee.core :as c]))

(defrecord KafkaProducer []
  
  c/Producer
  
  (send [this message]))
