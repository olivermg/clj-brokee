(ns clj-brokee.util
  (:refer-clojure :rename {pmap pmap-clj}))

(def pmap #?(:clj  pmap-clj
             :cljs map))

(defn run-async [f & args]
  #?(:clj  (future (apply f args))
     :cljs (js/setTimeout #(apply f args) 0))
  nil)
