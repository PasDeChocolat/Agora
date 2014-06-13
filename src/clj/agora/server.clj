(ns agora.server
  (:require [org.httpkit.server :as httpkit]
            [agora.handler :refer [app]]
            [ring.middleware.reload :as reload]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn render-app []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (str "<!DOCTYPE html>"
        "<html>"
        "<head>"
        "</head>"
        "<body>"
        "<div>"
        "<p><a href='https://github.com/PasDeChocolat/Agora'>Interesting things are afoot.</a></p>"
        "</div>"
        "<script src=\"js/agora.js\"></script>"
        "</body>"
        "</html>")})

(defn dev? [args] (some #{"-dev"} args))

(defn port [args]
  (if-let [port (first (remove #{"-dev"} args))]
    (Integer/parseInt port)
    3000))

(defn run-svr
  [port]
  (httpkit/run-server app {:port port}))

(defn -main
  "Start the server
   -dev for wrap-reload behavior
   port can be passed for port other than 3000."
  [& args]
  (if (dev? args) (reload/wrap-reload app) app)
  (let [p (port args)]
    (timbre/info "starting server on port" p)
    (run-svr p)))
