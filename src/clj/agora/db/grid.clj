(ns agora.db.grid
  (:require
   [agora.db.query :as aq]
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

(defn create-default-grid
  []
  @(d/transact conn [{:db/id (d/tempid :grid-part)
                     :grid/name DEFAULT-GRID-NAME}]))

(defn default-grid
  "The default grid entity"
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
  [& args]
  (apply str (interpose " " args)))

(defn mark-point
  "Set a magnitude for a grid point"
  [{:keys [x y]} mag]
  @(d/transact conn [{:db/id (d/tempid :grid-part)
                      :point/xy (point-key x y)
                      :point/magnitude mag}]))

(defn magnitude-at
  "Retrieve the magnitude of a point at a grid location"
  [{:keys [x y]}]
  (aq/maybe-r
   (d/q '[:find ?magnitude
          :in $ ?xy
          :where
          [?point :point/xy ?xy]
          [?point :point/magnitude ?magnitude]]
        (d/db conn)
        (point-key x y))))
