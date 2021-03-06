(ns agora.core
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true])
  (:require-macros
   [dommy.macros :refer [node sel sel1]]))

(enable-console-print!)

(def app-state (atom {:text "Hello world!"}))

(when (sel1 :#app)
  (om/root
   (fn [app owner]
     (dom/h1 nil (:text app)))
   app-state
   {:target (. js/document (getElementById "app"))}))
