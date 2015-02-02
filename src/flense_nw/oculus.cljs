(ns flense-nw.oculus
  (:require [clojure.string :as str]
            [flense.actions.completions :as completions]
            [flense.actions.history :as hist]
            [flense.layout :as layout]
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
  (let [height (* (- max-depth depth) step-height)
        push (* depth step-height)
        floor (* max-depth step-height)]
    (dom/div {:class "cube"
              :style {:-webkit-transform (str "translateZ(-" (px push) ") translateZ(" (px floor) ")")}}
      (dom/div {:class "cube-content"} content (dom/span {:class "cube-before"
                                                          :style {:width (px height)}} ""))
      (dom/div {:class "cube-after"
                :style {:height (px height)}}
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
                                 :style {:max-width (str (/ (:max-width form) 2) "rem")}} (:text form)))))

(defcomponent delimiter [token owner opts]
  (render [_]
    (if (not (:seq (:classes token)))
      (cube (depth token) (dom/span {:class (str/join " " (map name (:classes token)))}
                            (:text token))))))

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
  (render [_]
    (let [{:keys [tree]} (z/assoc document :selected? true)]
      (dom/div {:class "oculus"}
        (dom/div {:class "perspective"}
          (om/build-all top-level-form (:children tree)
            {:opts (-> opts (update :line-length (fnil identity 72)))}))))))
