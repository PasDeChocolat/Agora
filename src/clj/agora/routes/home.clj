(ns agora.routes.home
  (:require [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [file-response]]))

(defroutes home-routes
  (GET "/" []
       (file-response "canvas.html" {:root "resources/public"})))
