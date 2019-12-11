(ns clj-brokee.hub.core2)

(declare handle)

(defn make-broker []
  {:clients (atom {})})

(defn emit [{:keys [clients] :as broker} client-id message]
  (dorun
   (map (fn [client]
          (handle client message))
        (-> (dissoc @clients client-id)
            vals))))


(defn make-client [handler]
  {:id      (rand-int 2000000000)
   :handler handler
   :broker  nil})

(defn connect! [{:keys [id] :as client} {:keys [clients] :as broker}]
  (let [client (assoc client :broker broker)]
    (swap! clients assoc id client)
    client))

(defn publish [{:keys [id broker] :as client} message]
  (emit broker id message))

(defn handle [{:keys [handler] :as client} message]
  (handler message))


#_(let [b  (make-broker)
        c1 (-> (make-client #(println "CLIENT1" %))
               (connect! b))
        c2 (-> (make-client #(println "CLIENT2" %))
               (connect! b))
        c3 (-> (make-client #(println "CLIENT3" %))
               (connect! b))]
    (publish c1 {:x 123})
    (publish c3 {:y 321}))
