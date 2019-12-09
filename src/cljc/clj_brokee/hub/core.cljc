(ns clj-brokee.hub.core)

(defrecord Hub [listeners])

(defn construct []
  (map->Hub {:listeners (atom [])}))

(defn emit-raw [{:keys [listeners] :as this} message]
  (dorun (map (fn [{:keys [handler] :as listener}]
                (try
                  (handler message)
                  (catch #?(:clj Throwable :cljs :default) e
                    (println "WARNING: handler threw error" e))))
              @listeners)))

(defn emit [this message]
  (emit-raw this {:message message}))

(defn listen-raw [{:keys [listeners] :as this} handler]
  (swap! listeners conj {:handler handler}))

(defn listen [this handler]
  (listen-raw this #(handler (some-> % :message))))
