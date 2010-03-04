(ns com.nwalex.sponge.gui
  (:require
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.core :as core]))

(declare action-map)

;; the currently running server
(def current-server (atom nil))
(def gui-frame (atom nil))

(defn- set-new-atom [old new]
  new)

(defn- toggle-action [action]
  (.setEnabled action (not (.isEnabled action))))

(defn- toggle-started []
  (toggle-action (:stop-server action-map))
  (toggle-action (:start-server action-map)))

(defn- start-server []
  (swap! current-server set-new-atom
         (server/start (server/make-server
                        8139
                        "http://localhost:8141")))
  (toggle-started))

(defn- stop-server []  
  (server/stop @current-server)
  (toggle-started)
  (swap! current-server set-new-atom nil))

(defn- configure []
  (doto (com.nwalex.sponge.gui.ConfigurationDialog. @gui-frame true)
    (.setVisible true)))

(defn- exit [])

(defn- make-action [name f enabled]
  (doto (proxy [javax.swing.AbstractAction] [name]
          (actionPerformed [event] (f)))
    (.setEnabled enabled)))

(def action-map
     {:start-server (make-action "Start Server" start-server true)
      :stop-server (make-action "Stop Server" stop-server false)
      :configure (make-action "Configure" configure true)
      :exit (make-action "Exit" exit true)})

(def sponge-controller
     (proxy [com.nwalex.sponge.gui.SpongeGUIController] []
                     (getStartServerAction [] (:start-server action-map))
                     (getStopServerAction [] (:stop-server action-map))
                     (getConfigureAction [] (:configure action-map))
                     (getExitAction [] (:exit action-map))))

(defn -main [& args]
  (swap! gui-frame set-new-atom
         (doto (com.nwalex.sponge.gui.SpongeGUI. sponge-controller)
           (.setVisible true)
           (.setTitle "Sponge"))))
