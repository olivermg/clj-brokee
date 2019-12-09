(ns clj-brokee.core-test
  (:require [clojure.test :refer :all]
            [clj-brokee.hub.core :as h]
            [clj-brokee.hub.transport.direct :as td]))

(deftest hub
  (let [h  (h/construct)
        a1 (atom [])
        a2 (atom [])]
    (h/listen h (fn [msg]
                  (swap! a1 conj msg)))
    (h/listen h (fn [msg]
                  (swap! a2 conj msg)))
    (h/emit h :x)
    (h/emit h :y)
    (is (= [:x :y] @a1))
    (is (= [:x :y] @a2))))

(deftest direct-transport
  (let [h1  (h/construct)
        h2  (h/construct)
        t   (-> (td/construct h1 h2)
                (td/start))
        a11 (atom [])
        a12 (atom [])
        a21 (atom [])
        a22 (atom [])]
    (h/listen h1 (fn [msg]
                   (swap! a11 conj msg)))
    (h/listen h1 (fn [msg]
                   (swap! a12 conj msg)))
    (h/listen h2 (fn [msg]
                   (swap! a21 conj msg)))
    (h/listen h2 (fn [msg]
                   (swap! a22 conj msg)))
    (h/emit h1 :x1)
    (h/emit h1 :y1)
    (h/emit h2 :x2)
    (h/emit h2 :y2)
    (is (= [:x1 :y1 :x2 :y2] @a11))
    (is (= [:x1 :y1 :x2 :y2] @a12))
    (is (= [:x1 :y1 :x2 :y2] @a21))
    (is (= [:x1 :y1 :x2 :y2] @a22))
    (td/stop t)))
