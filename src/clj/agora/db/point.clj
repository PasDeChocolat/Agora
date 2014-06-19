(ns agora.db.point
  (:require
   [agora.db.conn :refer [conn] :as conn]
   [agora.db.result :as ar]
   [datomic.api :as d]
   [clojure.string :as string]))

(def KEY-DELIM " ")

(defn point-key
  "Create a point entity key from x and y values
   Extendable to more args (coords)"
  ([{:keys [x y]}]
     (point-key x y))
  ([x y & more-args]
     (let [coords (if (seq more-args)
                    (apply conj [x y] more-args)
                    [x y])]
       (apply str (interpose KEY-DELIM coords)))))

(defn key->xy
  [key]
  (mapv #(Integer/parseInt %)
        (string/split key (re-pattern KEY-DELIM))))

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

(defn magnitude
  [point]
  (get (conn/real point) :point/magnitude))
