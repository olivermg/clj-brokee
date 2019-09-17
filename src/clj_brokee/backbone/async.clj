(ns clj-brokee.backbone.async
  (:require [clojure.core.async :as a]
            [clj-brokee.backbone :as b]))

(defrecord CoreAsyncBackbone [ch mix mult]
  
  b/Backbone
  
  (connect [this broker]
           ))

(defn create []
  (let [ch   (a/chan)
        mix  (a/mix ch)
        mult (a/mult ch)]
    (map->CoreAsyncBackbone {:ch   ch
                             :mix  mix
                             :mult mult})))