; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.gui-controller
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.core :as core]
   [com.nwalex.sponge.gui-filters :as filters]
   [com.nwalex.sponge.table-model :as model]
   [com.nwalex.sponge.session :as session]
   [com.nwalex.sponge.label-controller :as label]
   [com.nwalex.sponge.config-controller :as config]
   [com.nwalex.sponge.exchange :as exchange]
   [clojure.contrib.swing-utils :as swing]
   [clojure.contrib.logging :as log]
   [clojure.contrib.java-utils :as util]))

(declare action-map)

(defn- log-action [f]
  (log/info (format "ACTION PERFORMED: %s" f)))

(defn- make-action [name f enabled]
  (doto (proxy [javax.swing.AbstractAction] [name]          
          (actionPerformed [event]
                           (log-action f)
                           (f event)))
    (.setEnabled enabled)))

(defn- make-safe-action [session name f enabled]
  (com.nwalex.sponge.gui.SafeAction.
   (make-action name f enabled)
   (state/gui session)))

(defn- make-multi-row-action [f table]
  (proxy [com.nwalex.sponge.gui.JXTableMultiRowAction] [table]
    (multiRowActionPerformed [indices]
                             (log-action f)
                             (f indices))))

(defn- make-single-row-action [f table]
  (proxy [com.nwalex.sponge.gui.JXTableSingleRowAction] [table]
    (singleRowActionPerformed [row]
                              (log-action f)
                              (f row))))

(defn- toggle-action [action]
  (.setEnabled action (not (.isEnabled action))))

(defn- toggle-started [session]
  (let [action-map (session/action-map session)]
    (toggle-action (:stop-server action-map))
    (toggle-action (:start-server action-map))
    (toggle-action (:configure action-map))
    (toggle-action (:load action-map))))

(defn- stop-server [session event]  
  (server/stop (state/current-server session))
  (toggle-started session)
  (state/set-current-server! session nil))

(defn- start-server [session event]
  (let [config (state/config session)]
    (state/set-current-server! session
                               (server/start
                                (server/make-server
                                 (:port config)
                                 (:target config)
                                 :request-filters
                                 (filters/get-request-filters-for-mode
                                  session (state/get-mode session))
                                 :response-filters
                                 (filters/get-response-filters-for-mode
                                  session (state/get-mode session))))))
  (toggle-started session))

(defn- start-repl [session event]
  (let [action-map (session/action-map session)]
    (toggle-action (:start-repl action-map))
    (state/repl-started! session)
    (future   
     (core/start-repl 4006))))

(defn- exit [event]
  (System/exit 1))

(defn- resend-request [session row]
  (let [exchange (model/get-exchange-for-row session row)]
    (future (server/resend-request exchange (state/current-server session))))
  0)

(defn- resend-all-requests [session rows]
  (amap rows idx ret (resend-request session (aget rows idx))))

(defn- wrap-session-action [session f event]
  (f event)
  (.setEnabled (:save (session/action-map session)) (session/has-file session)))

(defn action-map-fn
  "Designed to be called as a partial to bake in the session"
  [session]
  {:start-server (make-safe-action session
                                   "Start Server" (partial start-server session) true)
   :stop-server (make-safe-action session
                                  "Stop Server" (partial stop-server session) false)
   :configure (make-safe-action session
                                "Configure" (partial config/configure session) true)
   :exit (make-safe-action session "Exit" exit true)
   :start-repl (make-safe-action session "Start Repl" (partial start-repl session) true)
   :load (make-safe-action session "Load Session..."
                           #(wrap-session-action session
                                                 (partial session/load-session! session)
                                                 %1) true)
   :save (make-safe-action session
                           "Save Session" (partial session/save-session session) false)
   :save-as (make-safe-action session "Save Session As..."
                              #(wrap-session-action
                                session
                                (partial session/save-session-as session) %1)
                              true)
   :resend-request (ref nil)
   :update-request (ref nil)
   :update-response (ref nil)})

(defn- make-table-action [session table key proxy-maker]
  (let [action-map (session/action-map session)]
    (if-not @(key action-map)    
      (dosync
       (ref-set (key action-map)
                (proxy-maker table))))
    @(key action-map)))

(defn- resend-request-proxy [session table]
  (proxy [com.nwalex.sponge.gui.JXTableMultiRowAction] [table]
    (multiRowActionPerformed [rows]
                             (log-action "resend-request")
                             (resend-all-requests session rows))
    (setEnabled [enabled] (proxy-super setEnabled
                                       (and enabled
                                            (server/running?
                                             (state/current-server session)))))))

(defn- save-body-action [session key table]
  (proxy [com.nwalex.sponge.gui.BodyPanel$SaveAction] [table]
    (saveText [text row]
              (log-action (format "save-body %s" key))
              (model/update-exchange-body! session text key row))))

(defn- resend-request-action [session table]
  (make-table-action session table :resend-request
                     (partial resend-request-proxy session)))

(defn- update-body-action [session table key action-key]
  (make-table-action session table action-key (partial save-body-action session key)))

(defn- sponge-controller-fn
  [session action-map]
     (proxy [com.nwalex.sponge.gui.SpongeGUIController] []
       (getStartServerAction [] (:start-server action-map))
       (getStopServerAction [] (:stop-server action-map))
       (getConfigureAction [] (:configure action-map))
       (getExitAction [] (:exit action-map))
       (getExchangeTableModel [] (session/table-model session))
       (getStartReplAction [] (:start-repl action-map))
       (getRequestDataForRow [row] (model/get-data-for-row session
                                                           row :request))
       (getResponseDataForRow [row] (model/get-data-for-row session
                                                            row :response))       
       (getLabelExchangeAction [table] (make-multi-row-action
                                        (partial label/do-label session) table))
       (getDeleteLabelAction [table] (make-multi-row-action
                                      (partial label/delete-label session) table))
       (getLoadAction [] (:load action-map))
       (getSaveAction [] (:save action-map))
       (getSaveAsAction [] (:save-as action-map))
       (getSetDefaultResponseAction [table]
                                    (make-multi-row-action
                                     (partial model/set-as-default-response! session)
                                     table))
       (getMode [] (state/get-mode session))
       (setMode [mode] (filters/set-mode session mode))
       (getDeleteRowAction [table] (make-multi-row-action
                                    (partial model/delete-rows! session) table))
       (getResendRequestAction [table] (resend-request-action session table))
       (getUpdateRequestBodyAction [table] (update-body-action session
                                                               table
                                                               :request
                                                               :update-request))
       (getUpdateResponseBodyAction [table] (update-body-action session
                                                                table
                                                                :response
                                                                :update-response))
       (getDuplicateRowAction [table] (make-multi-row-action
                                       (partial model/duplicate-rows! session)
                                       table))))


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

(defn- load-session [session props]
  (let [do-load-prop (.getProperty props "sponge.reload.previous" "false")]
    (if (= do-load-prop "true")
      (let [file (java.io.File. (.getProperty props "sponge.last.session" ""))]
        (if (and (not (nil? file)) (.exists file))
          (do
            (session/load-session-from-file! file)
            (.setEnabled (:save (session/action-map session))
                         (session/has-file session)))
          (log/info (format "Session file not found: %s" file))))
      (log/info "sponge.reload.previous not set. Not loading previous session"))))

(defn- load-config! [session]
  (try
   (let [config-props (load-config-files)]
     (load-session session config-props)
     (state/set-config! session
      (.getProperty config-props
                    "sponge.default.port" "8139")
      (.getProperty config-props
                    "sponge.default.target" "http://services.aonaware.com")))
   (catch Exception ex
     (log/warn (format "Failed to load config") ex))))

(defn- store-last-session-properties [session]
  (log/info "Storing properties prior to shutdown")
  (util/write-properties
   {"sponge.default.port" ((state/config session) :port)
    "sponge.default.target" ((state/config session) :target)
    "sponge.last.session" (session/get-session-file session)}
   (format "%s/config/last.sponge.properties" (System/getProperty "sponge.home"))))

(defn- create-shutdown-hook! [session]
  (.addShutdownHook (Runtime/getRuntime) (Thread.
                                          (partial store-last-session-properties session
                                                   ))))

(defn make-gui [& args]
  (let [session (session/make-session)
        action-map (action-map-fn session)
        gui-controller (sponge-controller-fn session action-map)]
    (session/init-gui! session gui-controller action-map)
    (session/init-table-model! session
                              (model/make-exchange-table-model session))
    (swing/do-swing
     (state/set-gui! session
      (com.nwalex.sponge.gui.SpongeGUI. gui-controller
                                        (filters/make-plugin-controller session)))
     (load-config! session)
     (.updateSelectedMode (state/gui session) gui-controller)
     (create-shutdown-hook! session)
     (.setVisible (state/gui session) true))))
