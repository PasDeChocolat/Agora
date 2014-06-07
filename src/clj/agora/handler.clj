(ns agora.handler
  (:require [compojure.core :refer [GET defroutes routes]]
            [compojure.handler :as compojure-handler]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [agora.routes.home :refer [home-routes]]
            [agora.routes.socket :refer [socket-routes]]
            [agora.middleware :refer [wrap-timbre]]
            [ring.util.response :refer [file-response]]))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/info "agora app started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "agora is shutting down..."))

(defroutes app-routes
  (route/resources "/") ; {:root "resources"})
  (route/not-found "Not Found"))

(def all-routes (routes socket-routes
                        home-routes
                        app-routes))
(def app
  (-> (compojure-handler/site #'all-routes)
      (wrap-timbre {})))
