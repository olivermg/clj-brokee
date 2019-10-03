(ns clj-brokee.bridge.kafka
  (:require [clj-brokee.bridge :as b]))

(defrecord KafkaBridge []

  b/Bridge

  (rx-ch [this]
    (throw (ex-info "not implemented yet" {})))

  (tx-ch [this]
    (throw (ex-info "not implemented yet" {}))))
