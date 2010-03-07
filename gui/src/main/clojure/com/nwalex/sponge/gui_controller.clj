(ns com.nwalex.sponge.gui-controller
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.core :as core]
   [com.nwalex.sponge.gui-filters :as filters]
   [com.nwalex.sponge.table-model :as model]))

(declare action-map config-controller)

(defn- make-action [name f enabled]
  (doto (proxy [javax.swing.AbstractAction] [name]
          (actionPerformed [event] (f)))
    (.setEnabled enabled)))

(defn- toggle-action [action]
  (.setEnabled action (not (.isEnabled action))))

(defn- toggle-started []
  (toggle-action (:stop-server action-map))
  (toggle-action (:start-server action-map))
  (toggle-action (:configure action-map)))

(defn- stop-server []  
  (server/stop (state/current-server))
  (toggle-started)
  (state/set-current-server! nil))

(defn- start-server []
  (let [config (state/config)]
    (state/set-current-server! (server/start
                                (server/make-server
                                 (:port config)
                                 (:target config)
                                 :request-filters [filters/display-exchange-filter]
                                 :response-filters [filters/display-exchange-filter]))))
  (toggle-started))

(defn- start-repl []
  (core/start-repl 4006)
  (toggle-action (:start-repl action-map)))

(defn- exit []
  (System/exit 1))

(defn- configure []
  (doto (com.nwalex.sponge.gui.ConfigurationDialog. (state/gui) true config-controller)
    (.setLocationRelativeTo (state/gui))
    (.setVisible true)))

(def action-map
     {:start-server (make-action "Start Server" start-server true)
      :stop-server (make-action "Stop Server" stop-server false)
      :configure (make-action "Configure" configure true)
      :exit (make-action "Exit" exit true)
      :start-repl (make-action "Start Repl" start-repl true)
      :clear-all (make-action "Clear All" model/clear true)})

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
       (getClearAllAction [] (:clear-all action-map))))

(defn make-gui [& args]
  (state/set-gui! (doto (com.nwalex.sponge.gui.SpongeGUI. sponge-controller)
           (.setVisible true)
           (.setTitle "Sponge"))))
