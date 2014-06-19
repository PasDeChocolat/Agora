(ns agora.db.frame
  (:require
   [agora.db.conn :as conn]
   [agora.db.grid :as grid]
   [agora.db.point :as point]
   [datomic.api :as d]))

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

(defn last-frame
  []
  (->> (map (comp conn/real first) (grid/grid-points))
       (map #(into {} %))
       (map (convert-fn convert-xy :point/xy))
       (map (convert-fn convert-mag :point/magnitude))))
