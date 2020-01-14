(ns clj-brokee.hub.middlewares)

(defrecord Message [meta data])

(def meta-wrap (fn [handler]
                 (fn [message]
                   (handler (map->Message {:meta {}
                                           :data message})))))

(def meta-unwrap (fn [handler]
                   (fn [{:keys [data] :as message}]
                     (handler data))))
