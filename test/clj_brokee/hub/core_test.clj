(ns clj-brokee.core-test
  (:require [clojure.test :refer :all]
            [clj-brokee.hub.core :as h]
            [clj-brokee.hub.transport.direct :as td]))

(deftest hub
  (let [h  (h/construct)
        a1 (atom [])
        a2 (atom [])
        a3 (atom [])
        c1 (h/plug-in h (fn [msg]
                          (swap! a1 conj msg)))
        c2 (h/plug-in h (fn [msg]
                          (swap! a2 conj msg)))
        c3 (h/plug-in h (fn [msg]
                          (swap! a3 conj msg)))]
    (h/emit c1 :x)
    (h/emit c2 :y)
    (h/emit c3 :z)
    (is (= [:y :z] @a1))
    (is (= [:x :z] @a2))
    (is (= [:x :y] @a3))))

(deftest direct-transport
  (let [h1  (h/construct)
        h2  (h/construct)
        t   (-> (td/construct h1 h2)
                (td/start))
        a11 (atom [])
        a12 (atom [])
        a13 (atom [])
        a21 (atom [])
        a22 (atom [])
        a23 (atom [])
        c11 (h/plug-in h1 (fn [msg]
                            (swap! a11 conj msg)))
        c12 (h/plug-in h1 (fn [msg]
                            (swap! a12 conj msg)))
        c13 (h/plug-in h1 (fn [msg]
                            (swap! a13 conj msg)))
        c21 (h/plug-in h2 (fn [msg]
                            (swap! a21 conj msg)))
        c22 (h/plug-in h2 (fn [msg]
                            (swap! a22 conj msg)))
        c23 (h/plug-in h2 (fn [msg]
                            (swap! a23 conj msg)))]
    (h/emit c11 :c11)
    (h/emit c12 :c12)
    (h/emit c13 :c13)
    (h/emit c21 :c21)
    (h/emit c22 :c22)
    (h/emit c23 :c23)
    (is (= [:c12 :c13 :c21 :c22 :c23] @a11))
    (is (= [:c11 :c13 :c21 :c22 :c23] @a12))
    (is (= [:c11 :c12 :c21 :c22 :c23] @a13))
    (is (= [:c11 :c12 :c13 :c22 :c23] @a21))
    (is (= [:c11 :c12 :c13 :c21 :c23] @a22))
    (is (= [:c11 :c12 :c13 :c21 :c22] @a23))
    (td/stop t)))
