(ns clj-brokee.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [go go-loop] :as a]
            [clj-brokee.context :as ctx]
            [clj-brokee.broker :as b]
            [clj-brokee.hub :as h]
            [clj-brokee.producer :as p]
            [clj-brokee.consumer :as c]))

(def message1 {:topic :topic1
               :foo   "foo"
               :bar   123
               :baz   [{:a 1.23}
                       {:b #{:x :y :z}}]})
(def message2 {:topic :topic1
               :foo   "foo2"
               :bar   1234
               :baz   [{:a 1.234}
                       {:b #{:x :y :z}}]})

(defn wait-for-channel [ch & {:keys [timeout]
                              :or   {timeout 2000}}]
  (some-> (a/alts!! [ch (a/timeout timeout)])
          first))

(deftest via-broker
  (let [broker   (-> (b/construct :topic)
                     (b/start))
        producer (b/producer broker)
        consumer (b/consumer broker :topic1)]
    (testing "send & receive message via broker"
      (future
        (Thread/sleep 100)
        (go (p/produce producer message1)
            (p/produce producer message2)))
      (is (= {:message message1
              :context {:consumed message1}}
             (wait-for-channel
              (c/with-consumed consumer msg
                {:message msg
                 :context ctx/*current-context*}))))
      (is (= {:message message2
              :context {:consumed message2}}
             (wait-for-channel
              (c/with-consumed consumer msg
                {:message msg
                 :context ctx/*current-context*})))))
    (Thread/sleep 1000)
    (b/stop broker)))

(deftest via-hub
  (let [broker1  (-> (b/construct :topic)
                     (b/start))
        broker2  (-> (b/construct :topic)
                     (b/start))
        hub      (-> (h/construct)
                     (h/start)
                     (h/connect broker1)
                     (h/connect broker2))
        producer (b/producer broker1)
        consumer (b/consumer broker2 :topic1)]
    (testing "send & receive message brokers & hub"
      (future
        (Thread/sleep 100)
        (go (p/produce producer message1)
            (p/produce producer message2)))
      (is (= {:message message1
              :context {:consumed message1}}
             (wait-for-channel
              (c/with-consumed consumer msg
                {:message msg
                 :context ctx/*current-context*}))))
      (is (= {:message message2
              :context {:consumed message2}}
             (wait-for-channel
              (c/with-consumed consumer msg
                {:message msg
                 :context ctx/*current-context*})))))
    (h/stop hub)
    (b/stop broker2)
    (b/stop broker1)))

#_(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
