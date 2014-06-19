(ns agora.routes.api
  (:require
   [compojure.core :refer [ANY GET defroutes]]
   [liberator.core :refer [resource defresource]]
   [clojure.data.json :as json]
   [agora.db.point :as point]))

#_(defroutes api-routes
  (ANY "/" [] (resource)))

(defresource grid [x y mag]
  :available-media-types ["application/json" "application/edn"]
  :handle-ok (fn [ctx]
               (let [media-type (get-in ctx [:representation :media-type])
                     data {:x x :y y :magnitude mag}]
                 (case media-type
                   "application/json" (json/write-str data) 
                   "application/edn" (pr-str data)
                   (pr-str data)))))

;; point xy -> xy,mag
;; point xy (time) -> xy,mag, as of time
;; grid -> all points
;; grid(time) -> all points, as of time

(defresource point [x y]
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
  (ANY "/api/point/:x/:y" [x y] (point x y))
  (ANY "/api/grid/:x/:y/:mag" [x y mag] (grid x y mag)))
