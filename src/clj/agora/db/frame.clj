(ns agora.db.frame
  (:require
   [agora.db.conn :as conn]
   [agora.db.grid :as grid]
   [agora.db.point :as point]
   [datomic.api :as d]
   [clj-time.core :as t]))

(defn convert-fn
  [f old-key]
  (fn [m]
    (let [new-part (f old-key m)]
      (-> (dissoc m old-key)
          (merge new-part)))))

(defn convert-xy
  [old-key m]
  (let [[x y] (point/key->xy (old-key m))]
    {:x x :y y}))

(defn convert-mag
  [old-key m]
  (let [mag (old-key m)]
    {:magnitude mag}))

(defn ent-map-grid->clj
  [ent-grid]
  (->> (map (comp conn/real first) ent-grid)
       (map #(into {} %))
       (map (convert-fn convert-xy :point/xy))
       (mapv (convert-fn convert-mag :point/magnitude))))

(defn last-frame
  []
  (ent-map-grid->clj (grid/grid-points)))

(defn frame-as-of-units-ago
  [units x]
  (let [now (t/now)
        ago (t/minus now (units x))
        db (d/as-of (d/db conn/conn)
                    (.toDate ago))]
    (ent-map-grid->clj (grid/grid-points {:db db}))))

(defn frame-as-of-minutes-ago
  [minutes]
  (frame-as-of-units-ago t/minutes minutes))

(defn frame-as-of-days-ago
  [days]
  (frame-as-of-units-ago t/days days))
