(ns agora.canvas
  (:require
   [cljs.core.async :refer [chan <! >! put!]]
   [cljs.reader :as reader]
   [dommy.core :as dommy])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [dommy.macros :refer [node sel sel1]]))

(def COLS 20)
(def ROWS 10)

(def P 10)
(def GW 40)
(def CW (+ (* COLS GW) (* 2 P) 1))
(def CH (+ (* ROWS GW) (* 2 P) 1))
(def targets (atom {}))
 
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

(defn update-canvas
  [{:keys [x y magnitude grid-name]}]
  (.log js/console "x y mag name " x " " y " " magnitude " " grid-name))

(defn add-message []
  (go
   (while true
     (let [msg      (<! receive)
           raw-data (.-data msg)
           data     (reader/read-string raw-data)]
       (.log js/console "message recieved: " (clj->js data))
       (if (= "datomic" (:name data))
         (update-canvas (:msg data)))))))

(defn make-receiver []
  (set! (.-onmessage ws) (fn [msg]
                           (put! receive msg)))
  (.log js/console "setup message receiver")
  (add-message)
  (.send ws {:msg "start polling" :name "canvas"}))

(defn draw-grid
  []
  (let [canvas (.getElementById js/document "grid-canvas")
        ctx (.getContext canvas "2d")
        fill-w (- GW 2)]
    (set! (.-fillStyle ctx) "rgb(0,0,0)")
    (doall
     (for [c (range 0 COLS)
           r (range 0 ROWS)
           :let [x (+ (inc P) (* c GW))
                 y (+ (inc P) (* r GW))]]
       (.fillRect ctx x y fill-w fill-w)))))

(defn init! []
  (make-sender)
  (make-receiver)
  (dommy/append! (sel1 :#grid) [:canvas {:width CW
                                         :height CH
                                         :id "grid-canvas"}])
  (draw-grid))

(def on-load
  (when (sel1 :#live-canvas)
    (.log js/console "loading canvas")
    (set! (.-onload js/window) init!)))
