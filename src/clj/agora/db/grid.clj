(ns agora.db.grid
  (:require
   [agora.db.query :as aq]
   [agora.db.result :as ar]
   [datomic.api :as d]))

(def conn nil)

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

(defn create-default-grid
  []
  @(d/transact conn [{:db/id (d/tempid :grid-part)
                     :grid/name DEFAULT-GRID-NAME}]))

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
  ([xy mag]
     (mark-point xy mag (default-grid)))
  ([{:keys [x y]} mag grid]
     @(d/transact conn [{:db/id (d/tempid :grid-part)
                         :point/xy (point-key x y)
                         :point/magnitude mag
                         :grid/_points grid}])))

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
