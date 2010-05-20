(ns com.nwalex.sponge.swing-utils
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [clojure.contrib.logging :as log]))

(defn toggle-action [action]
  (.setEnabled action (not (.isEnabled action))))

(defn log-action [f]
  (log/info (format "ACTION PERFORMED: %s" f)))

(defn- action-str [f name]
  (format "Name : %s, Function: %s" name f))

(defn make-action [name f enabled]
  (doto (proxy [javax.swing.AbstractAction] [name]          
          (actionPerformed [event]
                           (log-action (action-str f name))
                           (f event)))
    (.setEnabled enabled)))

(defn make-safe-action-with-gui [gui name f enabled]
  (com.nwalex.sponge.gui.SafeAction.
   name
   (make-action name f enabled)
   gui))

(defn make-safe-action [session name f enabled]
  (make-safe-action-with-gui (state/gui (:workspace session)) name f enabled))

(defn make-multi-row-action [f table]
  (proxy [com.nwalex.sponge.gui.JXTableMultiRowAction] [table]
    (multiRowActionPerformed [indices]
                             (log-action f)
                             (f indices))))

(defn make-single-row-action [f table]
  (proxy [com.nwalex.sponge.gui.JXTableSingleRowAction] [table]
    (singleRowActionPerformed [row]
                              (log-action f)
                              (f row))))
