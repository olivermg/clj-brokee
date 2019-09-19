(ns clj-brokee.backbone
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clj-brokee.core :as c]
            #?@(:clj  [[clojure.core.async :refer [go go-loop] :as a]]
                :cljs [[cljs.core.async :as a]])))

;;;                +--------+      +---------+        +--------+      +---------+
;;;           +--->+        | pch  |         |  pcch  |        | cch  |         +---->
;;; producers      |  pmix  +----->+  pmult  +------->+  cmix  +----->+  cpub   |      consumers
;;;           +--->+        |      |         |        |        |      |         +---->
;;;                +--------+      +-------+-+        +--+---+-+      +---------+
;;;                                        |             ^   ^
;;;                                        |             |   |
;;;                                        |             |   |
;;;                                        |             |   |        +---------+
;;;                                        |   pbch      |   |        |         +---->
;;;                                        +------------------------->+  bmult  |      backbones
;;;                                                      |   |        |         +---->
;;;                                                      |   |        +---------+
;;;           +---->-------------------------------------^   |
;;; backbones                                                |
;;;           +---->-----------------------------------------^

(defrecord Broker [topic-fn xf
                   pmix pch pmult pcch cmix cch cpub pbch bmult])

(defn produce [{:keys [pmix] :as this}]
  (let [tx-ch (a/chan)]
    (a/admix pmix tx-ch)
    tx-ch))

(defn subscribe [{:keys [cpub] :as this} topic & {:keys [group-id]}]
  (let [rx-ch (a/chan)]
    (a/sub cpub topic rx-ch)
    rx-ch))

(defn connect [{:keys [mix mult] :as this} other]
  (when (satisfies? c/Sender other)
    (a/admix mix (c/rx-ch other)))
  (when (satisfies? c/Receiver other)
    (a/tap mult (c/tx-ch other)))
  this)

(defn construct [topic-fn & {:keys [xf]}]  ;; xf could be filtering events by permissions etc.
  (map->Backbone {:topic-fn topic-fn
                  :xf xf}))

(defn start [{:keys [topic-fn xf] :as this}]
  (let [pch   (a/chan 1 xf)
        pcch  (a/chan)
        cch   (a/chan 1 xf)
        pbch  (a/chan)
        pmix  (a/mix pch)
        pmult (a/mult pch)
        cmix  (a/mix cch)
        cpub  (a/pub cch topic-fn)
        bmult (a/mult pbch)]
    (a/tap pmult pcch)
    (a/admix cmix pcch)
    (a/tap pmult pbch)
    (assoc this
           :pmix  pmix
           :pch   pch
           :pmult pmult
           :pcch  pcch
           :cmix  cmix
           :cch   cch
           :cpub  cpub
           :pbch  pbch
           :bmult bmult)))

(defn stop [{:keys [pch pcch cch pbch] :as this}]
  (a/close! pch)
  (a/close! pcch)
  (a/close! cch)
  (a/close! pbch)
  (assoc this
         :pmix  nil
         :pch   nil
         :pmult nil
         :pcch  nil
         :cmix  nil
         :cch   nil
         :cpub  nil
         :pbch  nil
         :bmult nil))


(comment
  
  (let [b1 (-> (construct)
               (start))
        b2 (-> (construct)
               (start))
        tx1 (c/tx-ch b1)
        rx1 (c/rx-ch b2)
        tx2 (c/tx-ch b2)
        rx2 (c/rx-ch b1)]
    (println "==================")
    (connect b1 b2)
    (go-loop [msg (a/<! rx1)]
      (if-not (nil? msg)
        (do (println "RX1" msg)
            (recur (a/<! rx1)))
        (println "RX1 STOP")))
    (go-loop [msg (a/<! rx2)]
      (if-not (nil? msg)
        (do (println "RX2" msg)
            (recur (a/<! rx2)))
        (println "RX2 STOP")))
    (Thread/sleep 500)
    (a/put! tx1 {:x "tx1" :y (rand-int 1000)})
    (Thread/sleep 500)
    (a/put! tx2 {:x "tx2" :y (rand-int 1000)})
    (Thread/sleep 1000)
    (stop b1)
    (stop b2))

  )