(ns clj-brokee.adapter.async
  (:require [clj-brokee.core :as c]))

;;; NOTE: this adapter doesn't make much sense, as it is just wrapping async with async. it's just for demo purposes.

(defrecord AsyncAdapter [rx-ch tx-ch commit-ch]

  c/ReceivingAdapter

  (send [this message]
    (a/>!! tx-ch message))

  c/SendingAdapter

  (recv [this]
    (a/<!! rx-ch))

  (commit [this message]
    (a/>!! commit-ch message)))
