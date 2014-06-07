(ns agora.routes.socket
  (:require [compojure.core :refer [GET defroutes]]
            [clojure.edn :as edn]
            [org.httpkit.server :as httpkit]
            [clj-time.local :as lt]
            [taoensso.timbre :as timbre
             :refer (trace debug info warn error fatal spy with-log-level)]))

(def channels (atom []))

(defn socket-handler [ring-request]
  ;; unified API for WebSocket and HTTP long polling/streaming
  (httpkit/with-channel ring-request channel    ; get the channel
    (if (httpkit/websocket? channel) ; if you want to distinguish them
      (httpkit/on-receive channel (fn [raw] ; two way communication
                                    (let [data (edn/read-string raw)
                                          name (:name data)
                                          msg  (:msg data)]
                                      (info "WebSocket: " data)
                                      (if (not (seq (filter #(= % channel) @channels)))
                                        (swap! channels #(conj %1 channel)))
                                      (doseq [c @channels]
                                        (httpkit/send!
                                         c (pr-str {:msg msg
                                                    :name name
                                                    :timestamp (.toString (lt/local-now))}))))))
      (httpkit/send! channel {:status 200
                              :headers {"Content-Type" "text/plain"}
                              :body    "Long polling?"}))))
(defroutes socket-routes
  (GET "/socket" [] socket-handler) ;; asynchronous(long polling)
  )
