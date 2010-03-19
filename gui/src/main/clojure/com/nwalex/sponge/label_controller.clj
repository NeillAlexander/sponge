(ns com.nwalex.sponge.label-controller
  (:require
   [com.nwalex.sponge.table-model :as model]
   [com.nwalex.sponge.gui-state :as state]))

(def controller
     (proxy [com.nwalex.sponge.gui.LabelDialogController] []
       (setLabel [label] (model/set-label-on-row label (state/current-row)))
       (getCurrentLabel [] (model/get-label-for-row (state/current-row)))))

(defn do-label [event]
  (doto (com.nwalex.sponge.gui.LabelDialog. (state/gui) true controller)
    (.setLocationRelativeTo (state/gui))
    (.setVisible true)))

(defn delete-label [event]
  (model/delete-label-on-row (state/current-row)))
