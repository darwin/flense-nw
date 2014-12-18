(ns flense-nw.cli
  (:require [cljs.core.async :as async]
            [clojure.string :as string]
            [om.core :as om :include-macros true]
            [om.dom :as dom]
            [phalanges.core :as phalanges]))

(defn- handle-key [command-chan ev]
  (case (phalanges/key-set ev)
    #{:enter}
      (let [input (.-target ev)]
        (async/put! command-chan (string/split (.-value input) #"\s+"))
        (set! (.-value input) "")
        (.blur input))
    #{:esc}
      (.. ev -target blur)
    ;else
      nil)
  (.stopPropagation ev))

(defn cli-view [_ owner]
  (om/component
    (dom/input
      #js {:id "cli"
           :onKeyDown #(handle-key (om/get-shared owner :command-chan) %)
           :onKeyPress #(.stopPropagation %)})))
