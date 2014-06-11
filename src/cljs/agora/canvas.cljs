(ns agora.canvas
  (:require
   [cljs.core.async :refer [chan <! >! put!]]
   [cljs.reader :as reader]
   [dommy.core :as dommy])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [dommy.macros :refer [node sel sel1]]))

(def send (chan))
(def receive (chan))

(def ws-url "ws://localhost:3000/agora-socket")
(def ws (new js/WebSocket ws-url))

(defn event-chan
  [c el type]
  (let [writer #(put! c el)]
    (dommy/listen! el type writer)
    {:chan c
     :unsubscribe #(.removeEventListener el type writer)}))

(defn make-sender []
  (event-chan send (sel1 "input#send-start") :click)
  (event-chan send (sel1 "input#send-stop") :click)
  (event-chan send (sel1 "input#send-msg") :click)
  (go
   (while true
     (let [el (<! send)]
       (.log js/console el)
       (when-let [msg (case (.-id el)
                        "send-start" "start polling"
                        "send-stop" "stop polling"
                        "send-msg" "a random message"
                        nil)]
         (.send ws {:msg msg :name "canvas"}))))))

(defn add-message []
  (go
   (while true
     (let [msg            (<! receive)
           _ (.log js/console "msg: " msg)
           raw-data       (.-data msg)
           data           (reader/read-string raw-data)]
       (.log js/console "message recieved: " (clj->js data))))))

(defn make-receiver []
  (set! (.-onmessage ws) (fn [msg]
                           (put! receive msg)))
  (.log js/console "setup message receiver")
  (add-message)
  (.send ws {:msg "start polling" :name "canvas"}))

(defn init! []
  (make-sender)
  (make-receiver))

(def on-load
  (when (sel1 :#live-canvas)
    (.log js/console "loading canvas")
    (set! (.-onload js/window) init!)))
