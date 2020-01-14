(ns clj-brokee.hub.core)

(def pmap* #?(:clj  pmap
              :cljs map))

(defprotocol Broker
  (connected! [this client])
  (emit [this client message]))

(defprotocol Client
  (id [this])
  (connect-to-broker! [this broker])
  (handle-outgoing [this message])
  (handle-incoming [this message]))

(defn publish [this message]
  (handle-outgoing this message))

(defn connect! [this broker]
  (let [this (connect-to-broker! this broker)]
    (connected! broker this)
    this))


(defrecord HubBroker [clients]

  Broker

  (connected! [this client]
    (swap! clients assoc (id client) client)
    this)

  (emit [this client message]
    (let [other-clients (-> @clients
                            (dissoc (id client))
                            vals)]
      (dorun
       (pmap* (fn [client]
                (try
                  (handle-incoming client message)
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

  (connect-to-broker! [this broker]
    (reset! (:broker this) broker)
    this)

  (handle-outgoing [this message]
    (emit @broker this message))

  (handle-incoming [this message]
    (handler message)))

(defn construct-client [handler]
  (map->HandlerClient {:id      (rand-int 2000000000)
                       :handler handler
                       :broker  (atom nil)}))
