(ns clj-brokee.util)

(defn run-async [f & args]
  #?(:clj  (future (apply f args))
     :cljs (js/setTimeout #(apply f args) 0))
  nil)
