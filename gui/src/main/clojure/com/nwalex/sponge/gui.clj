(ns com.nwalex.sponge.gui
  (:require
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.core :as core])
  (:gen-class :main true :name com.nwalex.sponge.Client))

(declare action-map)

;; the currently running server
(def current-server (atom nil))
(def gui-frame (atom nil))
(def port (ref 8139))
(def target (ref "http://localhost:8141"))

(defn- set-new-atom [old new]
  new)

(defn- set-config [new-port new-target]
  (dosync
   (commute port set-new-atom new-port)
   (commute target set-new-atom new-target)))

(defn- toggle-action [action]
  (.setEnabled action (not (.isEnabled action))))

(defn- toggle-started []
  (toggle-action (:stop-server action-map))
  (toggle-action (:start-server action-map))
  (toggle-action (:configure action-map)))

(defn- start-server []
  (swap! current-server set-new-atom
         (server/start (server/make-server
                        @port
                        @target)))
  (toggle-started))

(defn- stop-server []  
  (server/stop @current-server)
  (toggle-started)
  (swap! current-server set-new-atom nil))

(def config-controller
     (proxy [com.nwalex.sponge.gui.ConfigurationDialogController] []
       (setConfiguration [port target] (set-config port target))
       (getCurrentPort [] @port)
       (getCurrentTarget [] @target)))

(defn- configure []
  (doto (com.nwalex.sponge.gui.ConfigurationDialog. @gui-frame true config-controller)
    (.setLocationRelativeTo @gui-frame)
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
