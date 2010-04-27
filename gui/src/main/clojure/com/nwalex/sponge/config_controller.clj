; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

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
