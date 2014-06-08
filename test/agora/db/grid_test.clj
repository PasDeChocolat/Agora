(ns agora.db.grid-test
  (:require
   [expectations :refer :all]
   [agora.db.grid :refer :all :as grid]
   [agora.db.result :as ar]
   [datomic.api :as d]))

(defn create-empty-in-memory-db
  "Create a blank, in-memory Datomic DB"
  []
  (let [uri "datomic:mem://agora-test"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/datomic/agora_schema.edn")]
      (d/transact conn schema)
      conn)))

(defn in-context
  "rebind a var, expecations are run in the defined context"
  {:expectations-options :in-context}
  [work]
  (with-redefs [conn (create-empty-in-memory-db)]
    (work)))

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

;; Make sure there is only one point after updating its value

;; Retrieve all the points in a grid
