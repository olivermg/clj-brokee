(ns clj-brokee.bridge
  #_(:require [clojure.core.async :as a]))

;;; user/client stuff:

#_(defprotocol Producer
  (produce [this message]))

#_(defprotocol Consumer
  (consume [this])
  (commit [this message]))

#_(defprotocol Broker
  (producer [this])
  (consumer [this topic]))

;;; backend/hub stuff:

(defprotocol Bridge
  (tx-ch [this])
  (rx-ch [this]))
