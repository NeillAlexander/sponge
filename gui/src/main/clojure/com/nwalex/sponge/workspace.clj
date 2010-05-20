(ns com.nwalex.sponge.workspace
  (:require
   [com.nwalex.sponge.gui-controller :as session-controller]
   [com.nwalex.sponge.swing-utils :as swing]
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.core :as core]
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.persistence :as persistence]
   [com.nwalex.sponge.session :as session]
   [clojure.contrib.swing-utils :as cl-swing]
   [clojure.contrib.logging :as log]
   [clojure.contrib.java-utils :as util]))

(defn make-workspace
  "The data structure for the app as a whole"
  []
  (let [ws {:persistence-cookie (ref nil)
            :gui-frame-store (atom nil)
            :repl-running (atom false)
            :active-sessions (ref [])
            :action-map (ref nil)
            :config-props (ref nil)}
        cookie (persistence/make-cookie ws "Sponge Workspace Files" "spw")]
    (dosync (ref-set (:persistence-cookie ws) cookie))
    (add-watch (:current-file cookie)
               "wsfile"
               (fn [key ref old new]
                 (.setEnabled (:save-workspace @(:action-map ws)) true)))
    ws))

(defn- persistence-data [workspace]
  {:active-sessions (vec (map session/persistence-data
                              @(:active-sessions workspace)))})

(defn- create-session [workspace]  
  (let [session (session-controller/create-session workspace)]
    (dosync
     (commute (:active-sessions workspace) conj session))
    session))

(defn- reload-session [workspace data]
  (let [session (create-session workspace)]
    (session/load-data! session data)
    session))

(defn load-data! [workspace persistence-map]
  (let [session-data (:active-sessions persistence-map)]
    (log/info (format "%d sessions loaded in workspace" (count session-data)))    
    (map (partial reload-session workspace) session-data)))

(defn- start-repl [workspace event]
  (let [action-map @(:action-map workspace)]
    (swing/toggle-action (:start-repl action-map))
    (state/repl-started! workspace)
    (future   
     (core/start-repl 4006))))

(defn- load-properties [into from]
  (let [cl (.getClassLoader clojure.main)
        from-resource (.getResourceAsStream cl from)]
    (if-not (nil? from-resource)
      (try
       (.load into from-resource)
       (catch Exception e
         (log/warn (format "Failed to load config from: %s" from))))
      (log/info (format "File not found: %s" from)))
    into))

(defn- load-config-files []
  (let [config-props (java.util.Properties.)]
    (load-properties config-props "sponge.properties")
    (load-properties config-props "user.sponge.properties")
    (load-properties config-props "last.sponge.properties")))

(defn- load-config! [workspace]
  (try
   (let [config-props (load-config-files)]
     (dosync
      (ref-set (:config-props workspace) config-props)))
   (catch Exception ex
     (log/warn (format "Failed to load config") ex))))

(defn- convert-loaded-sessions [loaded-sessions]
  (if (> (count loaded-sessions) 0)
    (into-array (map session/gui-controller loaded-sessions))
    (make-array com.nwalex.sponge.gui.SpongeSessionController 0)))

(defn load-workspace-from-file! [workspace file]
  (log/info "Ready to load workspace from file")
  (convert-loaded-sessions (persistence/load-data @(:persistence-cookie workspace)
                                                  (partial load-data! workspace)
                                                  file)))

(defn- load-workspace [workspace]
  (log/info "Ready to load workspace")
  (convert-loaded-sessions (persistence/load-data @(:persistence-cookie workspace)
                                               (partial load-data! workspace))))

(defn- save-workspace [workspace event]
  (log/info "ready to save workspace")
  (persistence/save-data @(:persistence-cookie workspace)
                         (persistence-data workspace)))

(defn- save-workspace-as [workspace event]
  (log/info "ready to save workspace as")
  (persistence/save-data-as @(:persistence-cookie workspace)
                            (persistence-data workspace)))

(defn make-action-map [workspace]
  (let [gui @(:gui-frame-store workspace)
        action-map
        {:start-repl (swing/make-safe-action-with-gui gui "Start Repl"
                       (partial start-repl workspace) true)
         :save-workspace (swing/make-safe-action-with-gui gui "Save Workspace"
                           (partial save-workspace workspace) false)
         :save-workspace-as (swing/make-safe-action-with-gui gui "Save Workspace As"
                              (partial save-workspace-as workspace) true)}]
       (dosync
        (ref-set (:action-map workspace) action-map))
       action-map))

(defn- clean-up-session [session]
  (log/info "Cleaning up session...")
  (server/stop @(:current-server-store session)))

(defn- still-active? [controller-to-delete session-to-test]
  (if (= controller-to-delete @(:gui-controller session-to-test))
    (do
      (clean-up-session session-to-test)
      false)
    true))

(defn- delete-session [workspace session-controller]
  (log/info (format "delete-session called for %s" session-controller))
  ;; delete the session
  (let [active-sessions (:active-sessions workspace)]    
    (dosync
     (ref-set active-sessions
              (doall (filter #(still-active? session-controller %1)
                             @active-sessions))))))

(defn- no-previous-workspace-loaded [workspace msg]
  (log/info msg)
  (log/info "Initializing empty session...")
  (convert-loaded-sessions [(create-session workspace)]))

(defn- load-previous-workspace [workspace]
  (let [props @(:config-props workspace)
        do-load-prop (.getProperty props "sponge.reload.previous" "false")]
    (if (= do-load-prop "true")
      (let [file (java.io.File. (.getProperty props "sponge.last.workspace" ""))]
        (if (and (not (nil? file)) (.exists file))
          (do
            (load-workspace-from-file! workspace file))
          (no-previous-workspace-loaded workspace
           (format "Workspace file not found: %s" file))))
      (no-previous-workspace-loaded workspace
       "sponge.reload.previous not set. Not loading previous workspace"))))

(defn make-sponge-controller
  "The implementation of SpongeController"
  [workspace action-map]
  (proxy [com.nwalex.sponge.gui.SpongeController] []
    (getStartReplAction [] (:start-repl action-map))
    (createNewSession [] @(:gui-controller (create-session workspace)))
    (deleteSession [controller] (delete-session workspace controller))
    (initializeWorkspace [] (load-previous-workspace workspace))
    (loadWorkspace [] (load-workspace workspace))
    (getSaveWorkspaceAction [] (:save-workspace action-map))
    (getSaveWorkspaceAsAction [] (:save-workspace-as action-map))))

(defn- store-workspace-properties [workspace]
  (util/write-properties
   {"sponge.last.workspace" (persistence/current-file (:persistence-cookie workspace))}
   (format "%s/config/last.sponge.properties" (System/getProperty "sponge.home"))))

(defn- create-shutdown-hook! [workspace]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread.
                     (partial store-workspace-properties workspace))))

(defn make-gui [& args]
  (let [workspace (make-workspace)
        action-map (make-action-map workspace)
        controller (make-sponge-controller workspace action-map)]
    (load-config! workspace)
    (create-shutdown-hook! workspace)
    (cl-swing/do-swing
     (state/set-gui! workspace
                     (com.nwalex.sponge.gui.SpongeGUI. controller))
     (.initialize (state/gui workspace))
     (.setVisible (state/gui workspace) true))))
