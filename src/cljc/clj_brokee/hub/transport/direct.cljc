(ns clj-brokee.hub.transport.direct
  (:require [clj-brokee.hub.core :as h]
            #_[clj-brokee.hub.transport :as t]))

(defrecord DirectTransportClient [hub1 hub2
                                  client1 client2])

(defn construct [hub1 hub2]
  (map->DirectTransportClient {:hub1 hub1
                               :hub2 hub2}))

(defn start [{:keys [hub1 hub2] :as this}]
  (let [client2 (volatile! nil)
        client1 (h/construct-client #(h/publish @client2 %))]
    (vreset! client2 (h/construct-client #(h/publish client1 %)))
    (h/connect! client1 hub1)
    (h/connect! @client2 hub2)
    (assoc this
           :client1 client1
           :client2 @client2)))

(defn stop [this]
  ;;; TODO: implement
  this)
