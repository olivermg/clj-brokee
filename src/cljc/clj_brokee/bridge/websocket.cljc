(ns clj-brokee.bridge.websocket
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go-loop]]))
  (:require [clj-brokee.bridge :as b]
            [taoensso.sente :as ws]
            #?@(:clj  [[clojure.core.async :refer [go-loop] :as a]
                       #_[taoensso.sente.server-adapters.http-kit :as wsh]]
                :cljs [[cljs.core.async :as a]])))


(defrecord WebsocketBridge #?(:cljs [route-path
                                     mix mult ws-data]
                              :clj  [http-adapter-fn http-request-user-id-fn
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

   :clj  (defn construct [http-adapter-fn http-request-user-id-fn]
           (map->WebsocketBridge {:http-adapter-fn         http-adapter-fn
                                  :http-request-user-id-fn http-request-user-id-fn})))


(defn start [this]
  (let [{:keys [ch-recv send-fn] :as ws-data} (ws/make-channel-socket!
                                               #?(:clj  ((:http-adapter-fn this))
                                                  :cljs (:route-path this))
                                               #?(:clj  {:user-id-fn (:http-request-user-id-fn this)
                                                         #_(fn [req]
                                                             (println "USER-ID-FN" req)
                                                             :555-shoe)}
                                                  :cljs {:type :auto}))
        tx (a/chan)
        rx (a/chan)]

    (go-loop [msg (a/<! tx)]
      ;;; sending (local --ws-> remote)
      (when-not (nil? msg)
        #_(println "WS TX" msg)
        #?(:clj  (if-let [user-id (some-> msg meta ::user-id)]
                   (send-fn user-id [:websocket/event {:msg msg}])
                   (println "WARNING: discarding outbound ws message due to missing user-id"))
           :cljs (send-fn [:websocket/event {:msg msg}]))
        (recur (a/<! tx))))

    (go-loop [ws-msg (a/<! ch-recv)]
      ;;; receiving (remote --ws-> local)
      (when-not (nil? ws-msg)
        #_(println "WS RX" ws-msg)
        (when-let [msg (some-> ws-msg :event second :msg)]
          (let [msg #?(:clj  (with-meta msg
                               (assoc (meta msg)
                                      ::user-id (some-> ws-msg :uid)))
                       :cljs msg)]
            (a/>! rx msg)))
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
