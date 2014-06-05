(ns agora.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :as resources]
            [ring.util.response :as response]
            [ring.util.serve :only [serve] :as rus])
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

(defn handler [request]
  (if (= "/" (:uri request))
      (response/redirect "/index.html")
      (render-app)))

(def app 
  (-> handler
    (resources/wrap-resource "public")))

(defn -main [& args]
  (jetty/run-jetty app {:port 3000}))

(defn repl-serve []
  (rus/serve app))
