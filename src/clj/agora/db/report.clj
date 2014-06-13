(ns agora.db.report
  (:require
   [datomic.api :as d]
   [clojure.core.async :refer [<! >! chan go] :as async]))

(def tx-queue (atom nil))

(defn subscribe
  "Subscribe to transactions for a grid"
  [conn]
  (when (nil? @tx-queue)
    (reset! tx-queue (d/tx-report-queue conn))))

(defn unsubscribe
  [conn]
  (when @tx-queue
    (.removeTxReportQueue conn)
    (reset! tx-queue nil)))

(defn generic-tx-report
  [result]
  (d/q '[:find ?e ?aname ?v
       :in $ [[?e ?a ?v]]
       :where
       [?e ?a ?v]
       [?a :db/ident ?aname]]
     (:db-after result)
     (:tx-data result)))

;; Result report input looks like this for a mark-point txn:
;; ([277076930200556 :point/magnitude 99.8]
;;  [277076930200556 :point/xy 1 2]
;;  [13194139534315 :db/txInstant #inst "2014-06-11T03:23:34.813-00:00"]
;;  [277076930200554 :grid/points 277076930200556]
;; )
;; or for existing points:
;; #{[277076930200557 :point/magnitude 3.0]
;;   [13194139534334 :db/txInstant #inst "2014-06-12T05:17:20.447-00:00"]}
(defn magnitude-tx-report
  "Create report tuple for mark-point txns"
  [result]
  (if (not (nil? result))
    (d/q '[:find ?point ?xy ?mag ?grid-name
           :in $ [[?point ?a ?v]]
           :where
           [?a :db/ident :point/magnitude]
           [?point :point/xy ?xy]
           [?point :point/magnitude ?mag]
           [?grid :grid/points ?point]
           [?grid :grid/name ?grid-name]]
         (:db-after result)
         (:tx-data result))))

(defn point-data
  "Only retuns non-nil for mark-point txns"
  [result]
  (if-let [tup (first (seq (magnitude-tx-report result)))]
    (let [xy (nth tup 1)
          [x y] (map #(Integer/parseInt %) (clojure.string/split xy #" "))]
     {:x x :y y
      :point (nth tup 0)
      :magnitude (nth tup 2)
      :grid-name (nth tup 3)})
    nil))

(defn has-next-tx?
  []
  (and @tx-queue
       (.peek @tx-queue)))

(defn next-tx
  []
  (when (has-next-tx?)
    (.poll @tx-queue)))

(defn async-next-tx
  []
  (when (and @tx-queue
             (.peek @tx-queue))
    (let [c (chan)]
      (go (>! c (.poll @tx-queue)))
      (go (<! c)))))
