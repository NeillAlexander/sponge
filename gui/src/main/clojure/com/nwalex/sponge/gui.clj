(ns com.nwalex.sponge.gui
  (:require
   [com.nwalex.sponge.gui-controller :as controller])
  (:gen-class :main true :name com.nwalex.sponge.Client))

(defn -main [& args]
  (apply controller/make-gui args))
