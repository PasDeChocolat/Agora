(ns agora.db.grid
  (:require
   [agora.db.query :as aq]
   [agora.db.result :as ar]
   [datomic.api :as d]))

(def db-uri "datomic:free://localhost:4334/agora")

(defn initialize-db
  "Create a blank Datomic DB"
  [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)
        schema (load-file "resources/datomic/agora_schema.edn")]
    (d/transact conn schema)
    conn))

(def conn
  (try
    (d/connect db-uri)
    (catch Exception e
      (println "Initializing Datomic!")
      (initialize-db db-uri))))

(def DEFAULT-GRID-NAME "agora")

(defn grid
  ([] (grid DEFAULT-GRID-NAME))
  ([name]
     (d/q '[:find ?grid
            :in $ ?name
            :where [?grid :grid/name ?name]]
          (d/db conn)
          name)))

(defn grid-with
  "Return the grid ID which owns a point ID"
  [point]
  (ar/maybe-r
   (d/q '[:find ?grid
          :in $ ?point
          :where
          [?grid :grid/points ?point]]
        (d/db conn)
        point)))

(defn create-grid
  ([]
     (create-grid DEFAULT-GRID-NAME))
  ([name]
     @(d/transact conn [{:db/id (d/tempid :grid-part)
                         :grid/name name}])))

(defn create-default-grid []
  (create-grid))

(defn default-grid
  "The default grid entity ID"
  []
  (let [g (grid)]
    (if (seq g)
      (aq/only g)
      (do
        (create-default-grid)
        (aq/only (grid))))))

(defn point-key
  "Create a point entity key from x and y values
   Extendable to more args (coords)"
  ([{:keys [x y]}]
     (point-key x y))
  ([x y & more-args]
     (let [coords (if (seq more-args)
                    (apply conj [x y] more-args)
                    [x y])]
       (apply str (interpose " " coords)))))

(defn point-at
  "Return entity ID of grid point at location"
  [{:keys [x y]}]
  (ar/maybe-r
   (d/q '[:find ?point
          :in $ ?xy
          :where
          [?point :point/xy ?xy]]
        (d/db conn)
        (point-key x y))))

(defn mark-point
  "Set a magnitude for a grid point"
  [{:keys [x y grid] :or {grid (default-grid)}} mag]
  @(d/transact conn [{:db/id (d/tempid :grid-part)
                      :point/xy (point-key x y)
                      :point/magnitude mag
                      :grid/_points grid}]))

(defn magnitude-at
  "Retrieve the magnitude of a point at a grid location"
  ([xy]
     (magnitude-at (d/db conn) xy))
  ([db {:keys [x y]}]
     (ar/maybe-r
      (d/q '[:find ?magnitude
             :in $ ?xy
             :where
             [?point :point/xy ?xy]
             [?point :point/magnitude ?magnitude]]
           db
           (point-key x y)))))

(defn grid-points
  "Points in a grid (or all grids if none given)"
  ([]
     (aq/find-all-by (d/db conn) :point/xy))
  ([grid]
     (d/q '[:find ?point
            :in $ ?grid
            :where
            [?grid :grid/points ?point]]
          (d/db conn)
          grid)))
