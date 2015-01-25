(ns flense-nw.repl
  (:require [cemerick.piggieback :as piggieback]
            [weasel.repl.websocket :as weasel]))

(defn repl!
  "Starts a Clojurescript repl."
  []
  (piggieback/cljs-repl :repl-env (weasel/repl-env :ip "0.0.0.0" :port 9001)))
