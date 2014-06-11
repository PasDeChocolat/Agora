(ns agora.db.report-test
  (:require
   [clojure.test :refer :all]
   [agora.db.fixtures :refer [clean-db]]
   [agora.db.report :refer :all :as report]
   [datomic.api :as d]
   [clojure.core.async :refer [>!! <! <!! chan close! go thread] :as async]
   [agora.db.grid :refer [conn mark-point] :as grid]))

(use-fixtures :each clean-db)

#_(deftest channel-test
  (testing "Channel"
    (let [c (chan)]
      (thread (>!! c "hello"))
      (is (= "hello" (<!! c)))
      (close! c))))

#_(deftest dummy-test
  (testing "subscribes to channel for tx reports"
    (let [c (chan)]
      (thread (>!! c 2))
      (is (= 3 (<!! (go (inc (<! c)))))))))

;; queue returns new transactions values
(deftest subscribe-test
  (testing "subscribes to channel for tx reports"
    (try
      ;; create grid first, otherwise this will be the first txn
      (grid/create-default-grid)
      
      (report/subscribe conn)
      (mark-point  {:x 1 :y 2} 99.8)
      (let [c (report/async-next)
            _ (assert (not (nil? c)) "Channel should not be nil!")
            r (<!! c)
            pt-report (point-data r)]
        (is (= 1 (:x pt-report))))
      (finally 
        (report/unsubscribe conn)))))

;; next-tx on other kind of transaction (other than point update)
(deftest ignore-next-tx-test
  (testing "ignored txns are nil"
    (try
      (report/subscribe conn)
      (grid/create-default-grid)
      (let [c (report/async-next)
            r (<!! c)
            pt-report (point-data r)]
        (is (nil? (:x pt-report))))
      (finally 
        (report/unsubscribe conn)))))

;; subscribing to the correct queue
;; adds grid name to subscribers list
