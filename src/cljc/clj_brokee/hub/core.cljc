(ns clj-brokee.hub.core)

(defrecord Hub [listeners])

(defrecord Message [meta data])

(defn construct []
  (map->Hub {:listeners (atom [])}))

(defn emit [{:keys [listeners] :as this} message]
  (let [message (if-not (instance? Message message)
                  (map->Message {:meta {}
                                 :data message})
                  message)]
    (dorun (map (fn [{:keys [handler] :as listener}]
                  (try
                    (handler message)
                    (catch #?(:clj Throwable :cljs :default) e
                      (println "WARNING: handler threw error" e))))
                @listeners))))

(defn listen [{:keys [listeners] :as this} handler & {:keys [raw?]}]
  (let [handler (if-not raw?
                  #(handler (some-> % :data))
                  #(handler %))]
    (swap! listeners conj {:handler handler})))
