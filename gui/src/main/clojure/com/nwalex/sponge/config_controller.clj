(ns com.nwalex.sponge.config-controller
  (:require
   [com.nwalex.sponge.gui-state :as state]))

(def #^{:private true} controller
     (proxy [com.nwalex.sponge.gui.ConfigurationDialogController] []
       (setConfiguration [port target] (state/set-config! port target))
       (getCurrentPort [] (:port (state/config)))
       (getCurrentTarget [] (:target (state/config)))))

(defn configure [event]
  (doto (com.nwalex.sponge.gui.ConfigurationDialog. (state/gui) true controller)
    (.setLocationRelativeTo (state/gui))
    (.setVisible true)))
