(ns clj-brokee.hub.transport.direct
  (:require [clj-brokee.hub.core :as h]
            [clj-brokee.hub.transport :as t]))

(defn construct [hub hub2]
  (let [client2 (atom (constantly nil))]
    (-> (t/construct hub #(h/emit @client2 %))
        (assoc ::hub2 hub2
               ::client2 client2))))

(defn start [{:keys [::hub2] :as this}]
  (let [{:keys [client] :as this} (t/start this)]
    (update this ::client2 reset! (h/plug-in hub2 #(h/emit client %))))
  #_(h/listen hub2 #(when-not (= (some-> % :meta :transport/id) id)
                      (h/emit hub (assoc-in % [:meta :transport/id] id)))
              :raw? true))

(defn stop [this]
  ;;; TODO: implement
  (-> this
      (t/stop)))
