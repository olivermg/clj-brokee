(ns clj-brokee.hub.core)

(def pmap* #?(:clj  pmap
              :cljs map))

(defprotocol Broker
  (connect [this client])
  (emit [this client message]))

(defprotocol Client
  (id [this])
  (set-broker [this broker])
  (publish [this message])
  (deliver [this message]))


(defrecord HubBroker [clients]

  Broker

  (connect [this client]
    (swap! clients assoc (id client) client)
    (set-broker client this)
    nil)

  (emit [this client message]
    (let [other-clients (-> @clients
                            (dissoc (id client))
                            vals)]
      (dorun
       (pmap* (fn [client]
                (try
                  (deliver client message)
                  (catch #?(:clj Throwable :cljs :default) e
                    (println "WARNING: client threw error on deliver" e))))
              other-clients)))))

(defn construct []
  (map->HubBroker {:clients (atom {})}))


(defrecord HandlerClient [id handler
                          broker]

  Client

  (id [this]
    id)

  (set-broker [this broker]
    (reset! (:broker this) broker))

  (publish [this message]
    (emit @broker this message))

  (deliver [this message]
    (handler message)))

(defn construct-client [handler]
  (map->HandlerClient {:id      (rand-int 2000000000)
                       :handler handler
                       :broker  (atom nil)}))
