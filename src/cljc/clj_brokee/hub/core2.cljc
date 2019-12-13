(ns clj-brokee.hub.core2)

;;;               +--------------+
;;;               |              |
;;;               |              |
;;;        +------+    Broker    +------+
;;;        |      |              |      |
;;;        |   +->+              +<-+   |
;;;        |   |  +--------------+  |   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;; deliver|   |                    |   |deliver
;;;        |   |                    |   |
;;;        |   |emit            emit|   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;;        |   |                    |   |
;;;        v   |                    |   v
;;;      +-+---+--+              +--+---+-+
;;;      |        |              |        |
;;;      | Client |              | Client |
;;;      |        |              |        |
;;;      +--+--+--+              +--+--+--+
;;;         ^  |                    |  ^
;;;         |  |                    |  |
;;;  publish|  |handle-fn  handle-fn|  |publish
;;;         +  v                    v  +
;;;         User                    User

(defn make-broker []
  {:deliver-fns (atom {})})

(defn emit [{:keys [deliver-fns] :as broker} client-id message]
  (dorun
   (map (fn [deliver-fn]
          (deliver-fn message))
        (-> (dissoc @deliver-fns client-id)
            vals))))


(defn make-client [deliver-fn]
  (let [id (rand-int 2000000000)]
    {:id         id
     :deliver-fn deliver-fn
     :emit-fn    (fn [message]
                   (throw (ex-info "Client is not connected to a broker."
                                   {:client-id id
                                    :message   message})))}))

(defn publish [{:keys [emit-fn] :as client} message]
  (emit-fn message))


(defn connect! [{:keys [id deliver-fn] :as client} {:keys [deliver-fns] :as broker}]
  (swap! deliver-fns assoc id deliver-fn)
  (assoc client :emit-fn
         (fn [message]
           (emit broker id message))))


#_(let [b  (make-broker)
        c1 (-> (make-client #(println "CLIENT1" %))
               (connect! b))
        c2 (-> (make-client #(println "CLIENT2" %))
               (connect! b))
        c3 (-> (make-client #(println "CLIENT3" %))
               (connect! b))]
    (publish c1 {:x 123})
    (publish c3 {:y 321}))
