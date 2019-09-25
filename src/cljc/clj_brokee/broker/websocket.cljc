(ns clj-brokee.broker.websocket
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go-loop]]))
  (:require [clj-brokee.core :as c]
            [taoensso.sente :as ws]
            #?@(:clj  [[clojure.core.async :refer [go-loop] :as a]
                       [taoensso.sente.server-adapters.http-kit :as wsh]]
                :cljs [[cljs.core.async :as a]])))

#?(:clj
   (defn- make-ring-handler [{:keys [ws-data] :as this}]
     (let [get-handler (:ajax-get-or-ws-handshake-fn ws-data)
           post-handler (:ajax-post-fn ws-data)]
       (fn [{:keys [request-method] :as req}]
         (case request-method
           :get  (get-handler req)
           :post (post-handler req)
           {:status 405})))))


(defrecord Websocket [route-path
                      mix mult ws-data]

  wp/Routable

  (make-routes [this]
    [route-path #?(:clj  (make-ring-handler this)
                   :cljs :websocket)]))


(defrecord WebsocketBroker [route-path
                            mix mult ws-data]

  c/BackendBroker

  (tx-ch [{:keys [mix] :as this}]
    (let [ch (a/chan)]
      (a/admix mix ch)
      ch))

  (rx-ch [{:keys [mult] :as this}]
    (let [ch (a/chan)]
      (a/tap mult ch)
      ch)))


(defn construct [{:keys [route-path]
                  :or   {route-path "/websocket"}
                  :as   opts}]
  (map->WebsocketBroker {:route-path route-path}))

(defn start [{:keys [route-path] :as this}]
  (let [{:keys [ch-recv send-fn] :as ws-data} (ws/make-channel-socket!
                                               #?(:clj  (wsh/get-sch-adapter)
                                                  :cljs route-path)
                                               #?(:clj  {:user-id-fn (fn [req]
                                                                       (println "USER-ID-FN" req)
                                                                       :555-shoe)}
                                                  :cljs {:type :auto}))
        tx-ch (a/chan)]
    (go-loop [msg (a/<! tx-ch)]
      (when-not (nil? msg)
        ;;; TODO: send sane user-id on client-side:
        #?(:clj  (send-fn :555-shoe [:event/x {:msg msg}])
           :cljs (send-fn           [:event/x {:msg msg}]))
        (recur (a/<! tx-ch))))
    (assoc this
           :ws-data ws-data
           :mix     (a/mix tx-ch)
           :mult    (a/mult ch-recv))))

(defn stop [this]
  ;;; TODO: implement
  this)
