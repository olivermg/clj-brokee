(ns clj-brokee.hub.core2)

(declare handle)

(defn make-broker [emit-fn]
  {:emit-fn emit-fn
   :clients (atom {})})

(defn emit [{:keys [emit-fn clients] :as broker} client-id message]
  (dorun
   (map #(emit-fn % message)
        (-> (dissoc @clients client-id)
            vals))))

(defn make-hub-broker []
  (make-broker (fn [client message]
                 (handle client message))))


(defn make-client [handle-fn]
  {:id        (rand-int 2000000000)
   :handle-fn handle-fn
   :broker    nil})

(defn connect! [{:keys [id] :as client} {:keys [clients] :as broker}]
  (let [client (assoc client :broker broker)]
    (swap! clients assoc id client)
    client))

(defn publish [{:keys [id broker] :as client} message]
  (emit broker id message))

(defn handle [{:keys [handle-fn] :as client} message]
  (handle-fn message))

(defn make-handler-client [handler]
  (make-client handler))


#_(let [b  (make-hub-broker)
        c1 (-> (make-handler-client #(println "CLIENT1" %))
               (connect! b))
        c2 (-> (make-handler-client #(println "CLIENT2" %))
               (connect! b))
        c3 (-> (make-handler-client #(println "CLIENT3" %))
               (connect! b))]
    (publish c1 {:x 123})
    (publish c3 {:y 321}))
