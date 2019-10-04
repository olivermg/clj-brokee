(ns clj-brokee.bridge.websocket
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go-loop]]))
  (:require [clj-brokee.bridge :as b]
            [taoensso.sente :as ws]
            #?@(:clj  [[clojure.core.async :refer [go-loop] :as a]
                       #_[taoensso.sente.server-adapters.http-kit :as wsh]]
                :cljs [[cljs.core.async :as a]])))


(defrecord WebsocketBridge #?(:cljs [route-path
                                     mix mult ws-data]
                              :clj  [http-adapter-fn
                                     mix mult ws-data])

  b/Bridge

  (tx-ch [{:keys [mix] :as this}]
    (let [ch (a/chan)]
      (a/admix mix ch)
      ch))

  (rx-ch [{:keys [mult] :as this}]
    (let [ch (a/chan)]
      (a/tap mult ch)
      ch)))


#?(:cljs (defn construct [route-path]
           (map->WebsocketBridge {:route-path route-path}))

   :clj  (defn construct [http-adapter-fn]
           (map->WebsocketBridge {:http-adapter-fn http-adapter-fn})))


(defn start [this]
  (let [{:keys [ch-recv send-fn] :as ws-data} (ws/make-channel-socket!
                                               #?(:clj  ((:http-adapter-fn this))
                                                  :cljs (:route-path this))
                                               #?(:clj  {:user-id-fn (fn [req]
                                                                       (println "USER-ID-FN" req)
                                                                       :555-shoe)}
                                                  :cljs {:type :auto}))
        tx (a/chan)
        rx (a/chan)]
    (go-loop [msg (a/<! tx)]
      (when-not (nil? msg)
        #_(println "WS TX" msg)
        ;;; TODO: send sane user-id on server-side:
        #?(:clj  (send-fn :555-shoe [:websocket/event {:data msg}])
           :cljs (send-fn           [:websocket/event {:data msg}]))
        (recur (a/<! tx))))
    (go-loop [msg (a/<! ch-recv)]
      (when-not (nil? msg)
        #_(println "WS RX" msg)
        (when-let [data (some-> msg :event second :data)]
          (a/>! rx data))
        (recur (a/<! ch-recv))))
    (assoc this
           :ws-data ws-data
           :mix     (a/mix tx)
           :mult    (a/mult rx))))


(defn stop [this]
  ;;; TODO: implement
  this)


#?(:clj  (defn make-ring-handler [{:keys [ws-data] :as this}]
           (let [get-handler  (:ajax-get-or-ws-handshake-fn ws-data)
                 post-handler (:ajax-post-fn ws-data)]
             (fn [{:keys [request-method] :as req}]
               (case request-method
                 :get  (get-handler req)
                 :post (post-handler req)
                 {:status 405})))))
