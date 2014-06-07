(ns agora.handler
  (:require [compojure.core :refer [GET defroutes routes]]
            [compojure.handler :as compojure-handler]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [agora.routes.home :refer [home-routes]]
            [agora.middleware :refer [wrap-timbre]]))

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
  ;; (GET "/async" [] async-handler) ;; asynchronous(long polling)
  (route/resources "/") ; {:root "resources"})
  (route/not-found "Not Found"))

(def app
  (-> (compojure-handler/site
       (routes home-routes app-routes))
      (wrap-timbre {})))
