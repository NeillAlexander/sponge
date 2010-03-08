(ns com.nwalex.sponge.gui-controller
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.core :as core]
   [com.nwalex.sponge.gui-filters :as filters]
   [com.nwalex.sponge.table-model :as model]))

(declare action-map config-controller label-controller)

(defn- make-action [name f enabled]
  (doto (proxy [javax.swing.AbstractAction] [name]
          (actionPerformed [event] (f event)))
    (.setEnabled enabled)))

(defn- toggle-action [action]
  (.setEnabled action (not (.isEnabled action))))

(defn- toggle-started []
  (toggle-action (:stop-server action-map))
  (toggle-action (:start-server action-map))
  (toggle-action (:configure action-map)))

(defn- stop-server [event]  
  (server/stop (state/current-server))
  (toggle-started)
  (state/set-current-server! nil))

(defn- start-server [event]
  (let [config (state/config)]
    (state/set-current-server! (server/start
                                (server/make-server
                                 (:port config)
                                 (:target config)
                                 :request-filters [filters/display-exchange-filter]
                                 :response-filters [filters/display-exchange-filter]))))
  (toggle-started))

(defn- start-repl [event]
  (core/start-repl 4006)
  (toggle-action (:start-repl action-map)))

(defn- exit [event]
  (System/exit 1))

(defn- configure [event]
  (doto (com.nwalex.sponge.gui.ConfigurationDialog. (state/gui) true config-controller)
    (.setLocationRelativeTo (state/gui))
    (.setVisible true)))

(defn- do-label [event]
  (doto (com.nwalex.sponge.gui.LabelDialog. (state/gui) true label-controller)
    (.setLocationRelativeTo (state/gui))
    (.setVisible true)))

(defn- delete-label [event]
  (model/delete-label-on-row (state/current-row)))

(defn- update-row [row]
  (state/set-current-row! row)
  (.setEnabled (:label-action action-map) (state/row-selected))
  (.setEnabled (:delete-label action-map) (state/row-selected)))

(def action-map
     {:start-server (make-action "Start Server" start-server true)
      :stop-server (make-action "Stop Server" stop-server false)
      :configure (make-action "Configure" configure true)
      :exit (make-action "Exit" exit true)
      :start-repl (make-action "Start Repl" start-repl true)
      :clear-all (make-action "Clear All" model/clear true)
      :label-action (make-action "Attach Label..." do-label false)
      :delete-label (make-action "Delete Label" delete-label false)})

(def label-controller
     (proxy [com.nwalex.sponge.gui.LabelDialogController] []
       (setLabel [label] (model/set-label-on-row label (state/current-row)))
       (getCurrentLabel [] (model/get-label-for-row (state/current-row)))))

(def config-controller
     (proxy [com.nwalex.sponge.gui.ConfigurationDialogController] []
       (setConfiguration [port target] (state/set-config! port target))
       (getCurrentPort [] (:port (state/config)))
       (getCurrentTarget [] (:target (state/config)))))

(def sponge-controller
     (proxy [com.nwalex.sponge.gui.SpongeGUIController] []
       (getStartServerAction [] (:start-server action-map))
       (getStopServerAction [] (:stop-server action-map))
       (getConfigureAction [] (:configure action-map))
       (getExitAction [] (:exit action-map))
       (getExchangeTableModel [] (model/get-table-model))
       (getStartReplAction [] (:start-repl action-map))
       (getRequestDataForRow [row] (model/get-data-for-row row :request))
       (getResponseDataForRow [row] (model/get-data-for-row row :response))
       (getClearAllAction [] (:clear-all action-map))
       (getLabelExchangeAction [] (:label-action action-map))
       (setSelectedRow [row] (update-row row))
       (getDeleteLabelAction [] (:delete-label action-map))))

(defn make-gui [& args]
  (state/set-gui! (doto (com.nwalex.sponge.gui.SpongeGUI. sponge-controller)
           (.setVisible true)
           (.setTitle "Sponge"))))
