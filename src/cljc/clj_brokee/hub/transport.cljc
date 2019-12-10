(ns clj-brokee.hub.transport
  (:require [clj-brokee.hub.core :as h]))

(defrecord HubTransport [hub handler
                         client])

(defn construct [hub handler]
  (map->HubTransport {:hub     hub
                      :handler handler}))

(defn start [{:keys [hub handler] :as this}]
  (assoc this :client (h/plug-in hub handler))
  #_(h/listen hub #(when-not (= (some-> % :meta :transport/id) id)
                   (handler (assoc-in % [:meta :transport/id] id))
                   #_(h/emit hub2 (assoc-in % [:meta :transport/id] id)))
            :raw? true)
  #_(h/listen hub2 #(when-not (= (some-> % :meta :transport/id) id)
                      (h/emit hub1 (assoc-in % [:meta :transport/id] id)))
              :raw? true)
  #_this)

(defn stop [this]
  ;;; TODO: implement
  this)
