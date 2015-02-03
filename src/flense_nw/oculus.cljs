(ns flense-nw.oculus
  (:require [clojure.string :as str]
            [flense.actions.completions :as completions]
            [flense.actions.history :as hist]
            [flense-nw.oculus-layout :as layout]
            [flense.model :as model]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [xyzzy.core :as z]))

(def max-depth 10)
(def step-height 3)
(def ^:dynamic last-depth 0)

(defn px [x]
  (str x "px"))

(defn depth [token]
  (count (:path token)))

(defn cube [depth content]
  (let [bottom-thickness (* depth 2)
        height (- (* (- max-depth depth) step-height) bottom-thickness)
        push (* depth step-height)
        floor (* max-depth step-height)]
    (dom/div {:class (str "cube " (aget content "_store" "props" "className")) ; ugly
              :style {:-webkit-transform (str "translateZ(-" (px push) ") translateZ(" (px floor) ")")}}
      (dom/div {:class "cube-content"} content (dom/span {:class "cube-before"
                                                          :style {:width (px height)}} ""))
      (dom/div {:class "cube-after"
                :style {:height (px height)
                        :border-bottom-width (px bottom-thickness)}}
        ))))

(defcomponent atom* [form owner opts]
  (render [_]
    (cube (depth form) (dom/div {:class (cond-> (str "atom " (name (:type form)))
                                          (:head? form) (str " head")
                                          (:selected? form) (str " selected")
                                          (:collapsed-form form) (str " macroexpanded"))}
                         (dom/span (:text form))))))

(defcomponent stringlike [form owner opts]
  (render [_]
    (cube (depth form) (dom/div {:class (cond-> (str "stringlike " (name (:type form)))
                                          (:editing? form) (str " editing")
                                          (:selected? form) (str " selected"))
                                 } (dom/div {:class "stringlike-inner"} (:text form))))))

(defcomponent delimiter [token owner opts]
  (render [_]
    (cube (depth token) (dom/span {:class (str/join " " (map name (:classes token)))}
                          (:text token)))))

(defcomponent top-level-form [form owner opts]
  (render [_]
    (dom/div {:class "toplevel"}
      (for [line (layout/->lines form (:line-length opts))]
        (dom/div {:class "line"}
          (for [token line]
            (do
              (if (not (layout/spacer? token))
                (set! last-depth (depth token)))
              (condp #(%1 %2) token
                layout/spacer? (dom/span {:class "spacer"} (:text token))
                layout/delimiter? (om/build delimiter token {:opts opts})
                model/stringlike? (om/build stringlike token {:opts opts})
                (om/build atom* token {:opts opts})))))))))

(defcomponent editor [document owner opts]
  (did-mount [_]
    (js/Traqball. #js {:stage "traqball" :angles #js [30 10] :perspective 1200}))
  (render [_]
    (let [{:keys [tree]} (z/assoc document :selected? true)]
      (dom/div {:class "oculus" }
        (dom/div {:id "traqball"}
          (dom/div {:class "grid"}
            (om/build-all top-level-form (:children tree)
              {:opts (-> opts (update :line-length (fnil identity 72)))})))))))
