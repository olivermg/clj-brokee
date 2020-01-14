(ns clj-brokee.broker
  (:refer-clojure :rename {deliver deliver-clj}))


(defprotocol BrokerDeliverer
  (deliver [this receiver data]))


(defrecord Broker [listeners])

(defn listen [{:keys [listeners] :as broker} listener]
  (swap! listeners #(conj % listener))
  broker)

(defn emit [{:keys [listeners] :as broker} data]
  (doseq [listener listeners]
    (deliver broker listener data)))


(defn make-broker []
  (map->Broker {:listeners (atom [])}))
