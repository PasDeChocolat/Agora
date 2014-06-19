(ns agora.db.grid-test
  (:require
   [clojure.test :refer :all]
   [agora.db.fixtures :refer [clean-db]]
   [agora.db.conn :refer [conn]]
   [agora.db.grid :refer :all :as grid]
   [agora.db.point :as point]
   [agora.db.query :as query]
   [agora.db.result :as ar]
   [datomic.api :as d]))

(use-fixtures :each clean-db)

;; Default grid has default name
(deftest grid-default-name-test
  (testing "grid has default name"
    (let [grid-id (default-grid)
          grid-e (d/entity (d/db conn) grid-id)]
      (is (= grid/DEFAULT-GRID-NAME
             (get grid-e :grid/name))))))

;; Write to grid point and retrieve value (double)
(deftest mark-point-and-retrieve-value-test
  (testing "mark a point and retrieve its value"
    (let [loc {:x 10 :y 20}]
          (mark-point loc 99.9)
          (is (= 99.9 (magnitude-at loc))))))

;; Check values at different points in DB history
(deftest point-values-in-history-test
  (testing "point values in past history"
    (let [loc {:x 10 :y 20}
          tx (mark-point loc 99.9)]
      (is (nil? (magnitude-at (get tx :db-before) loc)))
      (is (= 99.9 (magnitude-at (get tx :db-after) loc))))))

;; Create point and make sure it belongs to the grid
(deftest point-belongs-to-grid-test
  (testing "point belongs to a grid"
    (let [loc {:x 11 :y 22}
          _ (mark-point loc 3.3)
          pt-id (point/point-at loc)
          grid-id (grid-with pt-id)]          
      (is (= grid/DEFAULT-GRID-NAME
             (ar/maybe (d/db conn) grid-id :grid/name))))))

;; Change a point's value
(deftest change-points-value-test
  (testing "changing a point's value"
    (let [loc {:x 4 :y 2}]
          (mark-point loc 1.0)
          (mark-point loc 2.0)
          (is (= 2.0 (magnitude-at loc))))))

;; Make sure there is only one point after updating its value
(deftest updating-same-point-test
  (testing "only one entity exists after updating a point"
    (let [loc {:x 4 :y 2}]
          (mark-point loc 1.0)
          (mark-point loc 2.0)
          (is (= 1 (count
                    (ar/find-all-by (d/db conn)
                                    :point/xy
                                    (point/point-key loc))))))))

;; Retrieve all the points in a grid
(deftest retrieving-all-points-in-a-grid-test
  (testing "mark points and retrive via the grid ref"
    (mark-point {:x 1 :y 2} 1.0)
    (mark-point {:x 2 :y 2} 2.0)
    (mark-point {:x 3 :y 2} 3.0)
    (is (= 3 (count
              (grid-points))))))

;; Mark points in separate grids
(deftest mark-points-in-separate-grid-test
  (testing "points belong to separate grids"
   (let [g-id #(last (first (:tempids %)))
         g1 (g-id (create-default-grid))
         g2 (g-id (create-grid "another-grid"))]
          (mark-point {:x 1 :y 2 :grid g1} 1.0)
          (mark-point {:x 3 :y 4 :grid g2} 2.0)
          (is (= 2 (+ (count (grid-points {:grid g1}))
                      (count (grid-points {:grid g2}))))))))
