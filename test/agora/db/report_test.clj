(ns agora.db.report-test
  (:require
   [clojure.test :refer :all]
   [agora.db.report :refer :all :as report]
   [datomic.api :as d]
   [clojure.core.async :refer [>!! <! <!! chan close! go thread] :as async]
   [agora.db.grid :refer [mark-point]]))

(deftest channel-test
  (testing "Channel"
    (let [c (chan)]
      (thread (>!! c "hello"))
      (is (= "hello" (<!! c)))
      (close! c))))

(deftest dummy-test
  (testing "subscribes to channel for tx reports"
    (let [c (chan)]
      (thread (>!! c 2))
      (is (= 3 (<!! (go (inc (<! c)))))))))

#_(deftest subscribe-test
  (testing "subscribes to channel for tx reports"
    (report/subscribe "agora" inc)
    (mark-point {:x 1 :y 2} 99.8)
    (let [r (<!! (go (report/next-tx)))]
      (is (= 1 (:x r))))))
