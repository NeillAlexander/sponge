(ns com.nwalex.sponge.workspace
  (:require
   [com.nwalex.sponge.gui-controller :as session-controller]
   [com.nwalex.sponge.swing-utils :as swing]
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.core :as core]
   [clojure.contrib.swing-utils :as cl-swing]
   [clojure.contrib.logging :as log]))

(defn make-workspace
  "The data structure for the app as a whole"
  []
  {:gui-frame-store (atom nil)
   :repl-running (atom false)
   :active-sessions (ref {})
   :action-map (ref nil)})

(defn- create-session [workspace]  
  (let [session-controller (session-controller/create-session workspace)]
    (log/info "TODO: add session to the workspace")
    session-controller))

(defn- start-repl [workspace event]
  (log/info (format "start-repl called with workspace: %s" workspace))
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
     ;(load-session session config-props)
     ;(state/set-config! session
     ; (.getProperty config-props
     ;               "sponge.default.port" "8139")
     ; (.getProperty config-props
     ;"sponge.default.target" "http://services.aonaware.com"))
     )
   (catch Exception ex
     (log/warn (format "Failed to load config") ex))))

(defn make-action-map [workspace]
  (let [action-map {:start-repl
                    (swing/make-safe-action-with-gui
                      @(:gui-frame-store workspace)
                      "Start Repl"
                      (partial start-repl workspace) true)}]
       (dosync
        (ref-set (:action-map workspace) action-map))
       action-map))

(defn make-sponge-controller
  "The implementation of SpongeController"
  [workspace action-map]
  (proxy [com.nwalex.sponge.gui.SpongeController] []
    (getStartReplAction [] (:start-repl action-map))
    (createNewSession [] (create-session workspace))
    (deleteSession [session-controller] "deleteSession called")))

(defn- store-workspace-properties [workspace]
  (log/info "TODO: Store workspace properties prior to shutdown")
  ;(util/write-properties
  ; {"sponge.default.port" ((state/config session) :port)
  ;  "sponge.default.target" ((state/config session) :target)
  ;  "sponge.last.session" (session/get-session-file session)}
  ; (format "%s/config/last.sponge.properties" (System/getProperty "sponge.home")))
  )

(defn- create-shutdown-hook! [workspace]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread.
                     (partial store-workspace-properties workspace))))

(defn make-gui [& args]
  (let [workspace (make-workspace)
        action-map (make-action-map workspace)
        controller (make-sponge-controller workspace action-map)]
    (cl-swing/do-swing
     (state/set-gui! workspace
                     (com.nwalex.sponge.gui.SpongeGUI. controller))
     (load-config! workspace)
     (create-shutdown-hook! workspace)
     (.setVisible (state/gui workspace) true))))
