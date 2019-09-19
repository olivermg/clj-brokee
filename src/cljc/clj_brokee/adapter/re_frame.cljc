(ns clj-brokee.adapter.re-frame
  (:require [clj-brokee.core :as c]))

(defrecord ReFrameAdapter [topic-fn sub-topics]

  c/ReceivingAdapter

  (send [this message]
    #_(rf/dispatch [(topic-fn message) message]))
  
  c/SendingAdapter
  
  (recv [this]
        )
  
  (commit [this message]
          ))

(defn construct [received-fn topic-fn
                 & {:keys [sub-topics]}]
  #_(doseq [t sub-topics]
    @(rf/subscribe [t] (received-fn msg))  ;; TODO: do something sane here if re-frame supports something
    )
  (map->ReFrameAdapter {:received-fn received-fn
                        :topic-fn topic-fn
                        :sub-topics sub-topics}))

(defn start [this]
  this)

(defn stop [this]
  this)
