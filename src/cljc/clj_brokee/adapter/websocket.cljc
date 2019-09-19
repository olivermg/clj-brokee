(ns clj-brokee.adapter.websocket
  (:require [clj-brokee.core :as c]))

(defrecord WebsocketAdapter []
  ;;; will internally:
  ;;;  - dispatch to correct client via user-id-fn
  c/Sender
  (tx-ch [this])
  c/Receiver
  (rx-ch [this]))
