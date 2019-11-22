(ns clj-brokee.core-test
  (:require [clojure.test :refer :all]
            [clj-brokee.broker :as b]
            [clj-brokee.hub :as h]
            [clj-brokee.producer :as p]
            [clj-brokee.consumer :as c]))

(def message1 {:foo "foo"
               :bar 123
               :baz [{:a 1.23}
                     {:b #{:x :y :z}}]})
(def message2 {:foo "foo2"
               :bar 1234
               :baz [{:a 1.234}
                     {:b #{:x :y :z}}]})

(deftest via-broker
  (let [topic    :topic1
        broker   (-> (b/construct)
                     (b/start))
        producer (b/producer broker)
        consumer (b/consumer broker topic)]
    (testing "send & receive message via broker"
      (future
        (p/produce producer topic message1)
        (p/produce producer topic message2))
      (is (= message1
             (c/consume consumer)))
      (is (= message2
             (c/consume consumer))))
    (b/stop broker)))

(deftest via-hub
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
    (testing "send & receive message brokers & hub"
      (future
        (p/produce producer topic message1)
        (p/produce producer topic message2))
      (is (= message1
             (c/consume consumer)))
      (is (= message2
             (c/consume consumer))))
    (h/stop hub)
    (b/stop broker2)
    (b/stop broker1)))

#_(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
