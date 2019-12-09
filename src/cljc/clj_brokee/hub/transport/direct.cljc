(ns clj-brokee.hub.transport.direct
  (:require [clj-brokee.hub.core :as h]
            [clj-brokee.hub.transport :as t]))

(defn construct [hub hub2]
  (-> (t/construct hub #(h/emit hub2 %))
      (assoc :hub2 hub2)))

(defn start [{:keys [id hub hub2] :as this}]
  (h/listen hub2 #(when-not (= (some-> % :meta :transport/id) id)
                    (h/emit hub (assoc-in % [:meta :transport/id] id)))
            :raw? true)
  (-> this
      (t/start)))

(defn stop [this]
  ;;; TODO: implement
  (-> this
      (t/stop)))
