(ns clj-brokee.core-test
  (:require [clojure.test :refer :all]
            [clj-brokee.broker :as b]
            [clj-brokee.hub :as h]
            [clj-brokee.producer :as p]
            [clj-brokee.consumer :as c]))

(deftest hub
  (let [topic    :topic1
        broker1  (-> (b/construct)
                     (b/start))
        broker2  (-> (b/construct)
                     (b/start))
        hub      (-> (h/construct)
                     (h/start)
                     (h/connect broker1)
                     (h/connect broker2))
        producer (b/producer broker1)
        consumer (b/consumer broker2 topic)]
    (testing "via hub"
      (let [msg {:foo "foo"
                 :bar 123
                 :baz {:x 1.23}}]
        (p/produce producer topic msg)
        (is (= msg
               (c/consume consumer)))))
    (h/stop hub)
    (b/stop broker2)
    (b/stop broker1)))

#_(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
