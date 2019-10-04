(ns clj-brokee.hub
  (:require [clj-brokee.bridge :as b]
            #?@(:cljs [[cljs.core.async :as a]]
                :clj [[clojure.core.async :as a]])))

(defrecord Hub [mix ch mult])

(defn construct []
  (map->Hub {}))

(defn start [this]
  (let [ch (a/chan)]
    (assoc this
           :ch   ch
           :mix  (a/mix ch)
           :mult (a/mult ch))))

(defn stop [{:keys [ch] :as this}]
  (a/close! ch)
  (assoc this
         :ch   nil
         :mix  nil
         :mult nil))

(defn connect [{:keys [mix mult] :as this} bridge]
  (let [bridge-id (hash bridge)
        rx        (b/rx-ch bridge)
        rx-xf     (map (fn [msg]
                         #_(println "HUB RX" msg)
                         {::bridge-id bridge-id
                          ::payload   msg}))
        rx-marker (a/chan 1 rx-xf)
        tx        (b/tx-ch bridge)
        tx-xf     (comp (remove #(= (::bridge-id %)
                                    bridge-id))
                        #_(map #(do (println "HUB TX" %) %))
                        (map ::payload))
        tx-filter (a/chan 1 tx-xf)]
    (a/pipe rx rx-marker)
    (a/admix mix rx-marker)
    (a/pipe tx-filter tx)
    (a/tap mult tx-filter)
    this))
