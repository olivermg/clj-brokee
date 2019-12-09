(ns clj-brokee.hub.core)

(defrecord Hub [listeners])

(defn construct []
  (map->Hub {:listeners (atom [])}))

(defn emit [{:keys [listeners] :as this} message]
  (dorun (map (fn [{:keys [handler] :as listener}]
                (try
                  (handler message)
                  (catch #?(:clj Throwable :cljs :default) e
                    (println "WARNING: handler threw error" e))))
              @listeners)))

(defn listen [this handler]
  (update this :listeners swap! #(conj % {:handler handler})))
