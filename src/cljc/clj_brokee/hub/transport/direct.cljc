(ns clj-brokee.hub.transport.direct
  (:require [clj-brokee.hub.core :as h]))

(defrecord DirectHubTransport [id hub1 hub2])

(defn construct [hub1 hub2]
  (map->DirectHubTransport {:id   (rand-int 2000000000)
                            :hub1 hub1
                            :hub2 hub2}))

(defn start [{:keys [id hub1 hub2] :as this}]
  (h/listen hub1 #(when-not (= (some-> % :meta ::id) id)
                    (h/emit hub2 (assoc-in % [:meta ::id] id)))
            :raw? true)
  (h/listen hub2 #(when-not (= (some-> % :meta ::id) id)
                    (h/emit hub1 (assoc-in % [:meta ::id] id)))
            :raw? true)
  this)

(defn stop [this]
  ;;; TODO: implement
  this)
