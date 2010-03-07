(ns com.nwalex.sponge.gui
  (:require
   [com.nwalex.sponge.gui-state :as state])
  (:gen-class :main true :name com.nwalex.sponge.Client))

(defn -main [& args]
  (apply state/make-gui args))
