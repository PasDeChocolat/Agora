(ns agora.routes.socket
  (:require
   [compojure.core :refer [GET defroutes]]
   [clojure.edn :as edn]
   [org.httpkit.server :as httpkit]
   [clj-time.local :as lt]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]
   [clojure.core.async :refer [<! go thread] :as async]
   [agora.db.grid :as grid]
   [agora.db.report :as report]))

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

(def agora-channels (atom {}))
(def looping (atom false))
(defn should-loop-tx-push [channels]
  (some #(not (nil? %)) (vals @channels)))

(defn start-tx-push
  [channels]
  (when (not @looping)
    (reset! looping true)
    (report/subscribe grid/conn)
    (future
      (loop []
        (println "Looping with " (count (keys @channels)) " channels...")
        (doseq [[channel ch-on?] @channels]
          (when ch-on?
            (when-let [pt-data (report/point-data (report/next-tx))]
              (httpkit/send! channel
                             (pr-str {:msg pt-data
                                      :name "datomic"})
                             false))))
        (if (should-loop-tx-push channels)
          (do
            (Thread/sleep 5000)
            (recur))
          (do
            (println "will not recur.")
            (report/unsubscribe grid/conn)
            (reset! looping false)))))))

(defn agora-socket-handler [ring-request]
  ;; unified API for WebSocket and HTTP long polling/streaming
  (httpkit/with-channel ring-request channel    ; get the channel
    (if (httpkit/websocket? channel) ; if you want to distinguish them
      (do
        (if-not (find @agora-channels channel)
          (do
            (println "set up new closed channel")
            (swap! agora-channels assoc channel false)))
        (httpkit/on-receive channel (fn [raw] ; two way communication
                                     (let [data (edn/read-string raw)
                                           name (:name data)
                                           msg  (:msg data)]
                                       (info "WebSocket: " data)
                                       (case msg
                                         "start polling" (do
                                                           (swap! agora-channels assoc channel true)
                                                           (start-tx-push agora-channels))
                                         "stop polling" (swap! agora-channels assoc channel false)
                                         nil)
                                       (doseq [c (keys @agora-channels)]
                                         (if (get @agora-channels c)
                                           (httpkit/send!
                                            c (pr-str {:msg msg
                                                       :name name
                                                       :timestamp (.toString (lt/local-now))}
                                                      )
                                            false)
                                           (println "not sending: " msg))))))
        (httpkit/on-close channel (fn [status]
                                    (swap! agora-channels dissoc channel)
                                    (info "Channel closed: " status))))
      (httpkit/send! channel {:status 200
                              :headers {"Content-Type" "text/plain"}
                              :body    "Long polling?"}))))



(defroutes socket-routes
  (GET "/socket" [] socket-handler) ;; asynchronous(long polling)
  (GET "/agora-socket" [] agora-socket-handler)
  )
