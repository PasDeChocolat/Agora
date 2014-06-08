(ns agora.db.grid-test
  (:require
   [expectations :refer :all]
   [agora.db.grid :refer :all :as grid]
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
        (do
          (mark-point {:x 10 :y 20} 99.9)
          (magnitude-at {:x 10 :y 20})))

;; Create point and make sure it belongs to the grid

;; Change a point's value

;; Make sure there is only one point after updating its value

;; Retrieve all the points in a grid
