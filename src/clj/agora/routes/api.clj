(ns agora.routes.api
  (:require
   [compojure.core :refer [ANY GET defroutes]]
   [liberator.core :refer [resource defresource]]
   [clojure.data.json :as json]
   [agora.db.frame :as frame]
   [agora.db.point :as point]))

(defresource grid-resource
  []
  :available-media-types ["application/json" "application/edn"]
  :handle-ok (fn [ctx]
               (let [media-type (get-in ctx [:representation :media-type])
                     data (frame/last-frame)]
                 (case media-type
                   "application/json" (json/write-str data) 
                   "application/edn" (pr-str data)
                   (pr-str data)))))

(defn frame-data
  [unit n]
  (cond
   (re-matches #"(?i)^min" unit)
   (frame/frame-as-of-minutes-ago n)

   (re-matches #"(?i)^day" unit)
   (frame/frame-as-of-days-ago n)

   :else nil))

(defresource grid-ago-resource
  [unit n]
  :allowed-methods [:get]
  :available-media-types ["application/json" "application/edn"]
  :handle-ok (fn [ctx]
               (let [media-type (get-in ctx [:representation :media-type])
                     data (frame-data unit (Integer/parseInt n))]
                 (case media-type
                   "application/json" (json/write-str data) 
                   "application/edn" (pr-str data)
                   (pr-str data)))))

;; point xy -> xy,mag
;; point xy (time) -> xy,mag, as of time
;; grid -> all points
;; grid(time) -> all points, as of time

(defresource point-resource
  [x y]
  :allowed-methods [:get]
  :available-media-types ["application/json" "application/edn"]
  :handle-ok (fn [ctx]
               (let [media-type (get-in ctx [:representation :media-type])
                     point-loc {:x x :y y}
                     point (point/point-at point-loc)
                     data (assoc point-loc :magnitude (or (point/magnitude point) 0.0))]
                 (case media-type
                   "application/json" (json/write-str data) 
                   "application/edn" (pr-str data)
                   (pr-str data)))))


(defroutes api-routes
  (ANY "/foo" [] (resource :available-media-types ["text/html"]
                           :handle-ok (fn [ctx]
                                        (format "<html>It's %d milliseconds since the beginning of the epoch."
                                                (System/currentTimeMillis)))))
  (ANY "/api/point/:x/:y" [x y] (point-resource x y))
  (GET "/api/grid/ago/:unit/:n" [unit n] (grid-ago-resource unit n))
  (ANY "/api/grid" [] (grid-resource)))
