(ns clj-brokee.hub.transport
  (:require [clj-brokee.hub.core :as h]))

(defrecord HubTransport [id hub])

(defn construct [hub handler]
  (map->HubTransport {:id      (rand-int 2000000000)
                      :hub     hub
                      :handler handler}))

(defn start [{:keys [id hub handler] :as this}]
  (h/listen hub #(when-not (= (some-> % :meta :transport/id) id)
                   (handler (assoc-in % [:meta :transport/id] id))
                   #_(h/emit hub2 (assoc-in % [:meta :transport/id] id)))
            :raw? true)
  #_(h/listen hub2 #(when-not (= (some-> % :meta :transport/id) id)
                    (h/emit hub1 (assoc-in % [:meta :transport/id] id)))
            :raw? true)
  this)

(defn stop [this]
  ;;; TODO: implement
  this)
