(ns clj-brokee.core-test
  (:require [clojure.test :refer :all]
            [clj-brokee.hub.core :as h]))

(deftest test-1
  (let [h  (h/construct)
        a1 (atom [])
        a2 (atom [])]
    (h/listen h (fn [msg]
                  (swap! a1 #(conj % msg))))
    (h/listen h (fn [msg]
                  (swap! a2 #(conj % msg))))
    (h/emit h :x)
    (h/emit h :y)
    (is (= [:x :y] @a1))
    (is (= [:x :y] @a2))))
