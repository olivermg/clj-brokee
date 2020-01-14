(ns clj-brokee.broker.fn
  (:require [clj-brokee.broker :as b]))

(extend-type clj_brokee.broker.Broker

  b/BrokerDeliverer

  (deliver [this receiver data]
    (receiver data)))

(defn make-fn-broker []
  (b/make-broker))
