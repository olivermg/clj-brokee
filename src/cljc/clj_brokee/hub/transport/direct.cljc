(ns clj-brokee.hub.transport.direct
  (:require [clj-brokee.hub.core :as h]))

(defrecord DirectHubTransport [id hub1 hub2])

(defn construct [hub1 hub2]
  (map->DirectHubTransport {:id   (rand-int 2000000000)
                            :hub1 hub1
                            :hub2 hub2}))

(defn start [{:keys [id hub1 hub2] :as this}]
  (h/listen-raw hub1 #(when-not (= (some-> % ::id) id)
                        (h/emit-raw hub2 (assoc % ::id id))))
  (h/listen-raw hub2 #(when-not (= (some-> % ::id) id)
                        (h/emit-raw hub1 (assoc % ::id id))))
  this)

(defn stop [this]
  ;;; TODO: implement
  this)
