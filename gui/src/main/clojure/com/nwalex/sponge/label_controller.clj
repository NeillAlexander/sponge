; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

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
