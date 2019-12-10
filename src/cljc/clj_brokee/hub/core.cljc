(ns clj-brokee.hub.core)

(defrecord Hub [clients])

(defrecord Client [id hub handler])

(defn construct []
  (map->Hub {:clients (atom {})}))

(let [pmap #?(:clj  pmap
              :cljs map)]
  (defn emit* [{:keys [clients] :as this} client-id message]
    (let [other-clients (-> @clients
                            (dissoc client-id)
                            vals)]
      (dorun
       (pmap (fn [{:keys [handler] :as client}]
               (try
                 (handler message)
                 (catch #?(:clj Throwable :cljs :default) e
                   (println "WARNING: handler threw error" e))))
             other-clients)))
    nil))

(defn plug-in [{:keys [clients] :as this} handler]
  (let [client-id (rand-int 2000000000)
        client    (map->Client {:id      client-id
                                :hub     this
                                :handler handler})]
    (swap! clients assoc client-id client)
    client))

(defn plug-out [{:keys [clients] :as this} {:keys [id] :as client}]
  (swap! clients dissoc id)
  this)


(defn emit [{:keys [id hub] :as this} message]
  (emit* hub id message))
