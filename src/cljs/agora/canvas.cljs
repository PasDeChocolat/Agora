(ns agora.canvas
  (:require
   [cljs.core.async :refer [chan <! >! put!]]
   [cljs.reader :as reader]
   [dommy.core :as dommy])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [dommy.macros :refer [node sel sel1]]))


(def CANVAS-ID "grid-canvas")
(def COLS 20)
(def ROWS 10)

(def P 10)
(def GW 40)
(def FILL-GW (- GW 2))
(def CW (+ (* COLS GW) (* 2 P) 1))
(def CH (+ (* ROWS GW) (* 2 P) 1))

(def targets (atom {}))
(def looping-targets (atom nil))
(def COLOR-INC 5.0)

(def send (chan))
(def receive (chan))
(def canvas-chan (chan))

(def ws-url "ws://localhost:3000/agora-socket")
(def ws (new js/WebSocket ws-url))

(defn event-chan
  [c el type]
  (let [writer #(put! c %)]
    (dommy/listen! el type writer)
    {:chan c
     :unsubscribe #(.removeEventListener el type writer)}))

(defn send-on-socket
  [data]
  (.send ws (assoc data :name "canvas")))

(defn make-sender []
  (event-chan send (sel1 "input#send-start") :click)
  (event-chan send (sel1 "input#send-stop") :click)
  (event-chan send (sel1 "input#send-msg") :click)
  (go
   (while true
     (let [el (.-srcElement (<! send))]
       (when-let [data (case (.-id el)
                        "send-start" {:msg "start polling"    :type :poll}
                        "send-stop"  {:msg "stop polling"     :type :poll}
                        "send-msg"   {:msg "a random message" :type :chat}
                        nil)]
         (send-on-socket data))))))

(defn event-touch-location
  "Find a location of an event (click in canvas)
   from: http://answers.oreilly.com/topic/1929-how-to-use-the-canvas-and-draw-elements-in-html5/"
  [evt]
  (let [el (.-srcElement evt)
        x (or (.-pageX evt)
              (+ (.-clientX evt)
                 (aget js/document "body" "scrollLeft")
                 (aget js/document "documentElement" "scrollLeft")))
        y (or (.-pageY evt)
              (+ (.-clientY evt)
                 (aget js/document "body" "scrollTop")
                 (aget js/document "documentElement" "scrollTop")))] 
    [(- x (.-offsetLeft el))
     (- y (.-offsetTop el))]))

(defn xy->col-row
  [x y]
  (let [col (quot (max 0 (- x P)) GW)
        row (quot (max 0 (- y P)) GW)]
    [col row]))

(defn col-row->xy
  [c r]
  (let [x (+ (inc P) (* c GW))
        y (+ (inc P) (* r GW))]
    [x y]))

(defn rgb->css-rgb
  [r g b]
  (str "rgb(" (int r) "," (int g) "," (int b) ")"))

(defn fill-grid-point
  [col row [red green blue]]
  (let [canvas (.getElementById js/document CANVAS-ID)
        ctx (.getContext canvas "2d")
        color (rgb->css-rgb red green blue)
        [x y] (col-row->xy col row)]
    (set! (.-fillStyle ctx) color)
    (.log js/console "filling" col ":" row "=" color)
    (.fillRect ctx x y FILL-GW FILL-GW)))

(defn random-magnitude
  [min max]
  (+ min (* (rand) (- max min))))

(defn make-canvas-interaction []
  (event-chan canvas-chan (sel1 :#grid-canvas) :click)
  (go
   (while true
     (let [evt (<! canvas-chan)
           [x y] (event-touch-location evt)
           [col row] (xy->col-row x y)]
       (fill-grid-point col row [255 55 153])
       (send-on-socket {:msg  "mark-point"
                        :x col :y row
                        :magnitude (random-magnitude 40.0 100.0)
                        :type :update})))))

(defn agora-mag->rgb
  [magnitude]
  (let [g (* 255.0 (/ magnitude 100.0))]
    [g g g]))

(defn update-targets
  []
  (.log js/console "looping targets...")
  (.log js/console (clj->js @targets))
  (doseq [[col row :as k] (keys @targets)]
    (let [{:keys [current magnitude] :as pt-data} (@targets k)
          new-g (max magnitude (- current COLOR-INC))
          rgb (agora-mag->rgb new-g)]
      (fill-grid-point col row rgb)
      (if (= new-g magnitude)
        (swap! targets dissoc k)
        (swap! targets assoc k (assoc pt-data :current new-g)))))
  (when-not (seq (keys @targets))
    (js/clearInterval @looping-targets)
    (reset! looping-targets nil)))

(defn update-canvas
  [{:keys [x y magnitude grid-name]}]
  (.log js/console "x y mag name " x " " y " " magnitude " " grid-name)
  (swap! targets assoc [x y] {:magnitude magnitude
                              :current 100.0})
  (if (nil? @looping-targets)
    (reset! looping-targets (js/setInterval update-targets 1000))))

(defn add-message []
  (go
   (while true
     (let [msg      (<! receive)
           raw-data (.-data msg)
           data     (reader/read-string raw-data)]
       (.log js/console "message recieved: " (clj->js data))
       (if (= :db-txn (:type data))
         (update-canvas (:msg data)))))))

(defn make-receiver []
  (set! (.-onmessage ws) (fn [msg]
                           (put! receive msg)))
  (.log js/console "setup message receiver")
  (add-message)
  (.send ws {:msg "start polling" :name "canvas" :type :poll}))

(defn draw-grid
  []
  (let [canvas (.getElementById js/document "grid-canvas")
        ctx (.getContext canvas "2d")]
    (set! (.-fillStyle ctx) "rgb(0,0,0)")
    (doall
     (for [c (range 0 COLS)
           r (range 0 ROWS)
           :let [[x y] (col-row->xy c r)]]
       (.fillRect ctx x y FILL-GW FILL-GW)))))

(defn init! []
  (make-sender)
  (make-receiver)
  (dommy/append! (sel1 :#grid) [:canvas {:width CW
                                         :height CH
                                         :id "grid-canvas"}])
  (draw-grid)
  (make-canvas-interaction))

(def on-load
  (when (sel1 :#live-canvas)
    (.log js/console "loading canvas")
    (set! (.-onload js/window) init!)))
