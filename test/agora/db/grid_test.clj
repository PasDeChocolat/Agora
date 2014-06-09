(ns agora.db.grid-test
  (:require
   [expectations :refer :all]
   [agora.db.grid :refer :all :as grid]
   [agora.db.query :as query]
   [agora.db.result :as ar]
   [datomic.api :as d]))

;; Default grid has default name
(expect grid/DEFAULT-GRID-NAME
        (let [grid-id (default-grid)
              grid-e (d/entity (d/db conn) grid-id)]
          (get grid-e :grid/name)))

;; Write to grid point and retrieve value (double)
(expect 99.9
        (let [loc {:x 10 :y 20}]
          (mark-point loc 99.9)
          (magnitude-at loc)))

;; Check values at different points in DB history
(let [loc {:x 10 :y 20}]
  (expect (more-of tx
                   nil? (magnitude-at (get tx :db-before) loc)
                   99.9 (magnitude-at (get tx :db-after) loc))
          (mark-point loc 99.9)))

;; Create point and make sure it belongs to the grid
(expect grid/DEFAULT-GRID-NAME
        (let [loc {:x 11 :y 22}]
          (mark-point loc 3.3)
          (let [pt-id (point-at loc)
                grid-id (grid-with pt-id)]
            (ar/maybe (d/db conn) grid-id :grid/name))))

;; Change a point's value
(expect 2.0
        (let [loc {:x 4 :y 2}]
          (mark-point loc 1.0)
          (mark-point loc 2.0)
          (magnitude-at loc)))

;; Make sure there is only one point after updating its value
(expect 1
        (let [loc {:x 4 :y 2}]
          (mark-point loc 1.0)
          (mark-point loc 2.0)
          (count
           (ar/find-all-by (d/db conn)
                           :point/xy
                           (point-key loc)))))

;; Retrieve all the points in a grid
(expect 3
        (do
          (mark-point {:x 1 :y 2} 1.0)
          (mark-point {:x 2 :y 2} 2.0)
          (mark-point {:x 3 :y 2} 3.0)
          (count
           (grid-points))))

;; Mark points in separate grids
(expect 2
        (let [g-id #(last (first (:tempids %)))
              g1 (g-id (create-default-grid))
              g2 (g-id (create-grid "another-grid"))]
          (mark-point {:x 1 :y 2 :grid g1} 1.0)
          (mark-point {:x 3 :y 4 :grid g2} 2.0)
          (+ (count (grid-points g1))
             (count (grid-points g2)))))
