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
   [com.nwalex.sponge.gui-filters :as filters]
   [com.nwalex.sponge.table-model :as model]
   [com.nwalex.sponge.session :as session]
   [com.nwalex.sponge.label-controller :as label]
   [com.nwalex.sponge.config-controller :as config]
   [com.nwalex.sponge.exchange :as exchange]
   [com.nwalex.sponge.swing-utils :as swing]
   [clojure.contrib.logging :as log]
   [clojure.contrib.java-utils :as util]))

(declare action-map)

(defn- toggle-started [session]
  (let [action-map (session/action-map session)]
    (swing/toggle-action (:stop-server action-map))
    (swing/toggle-action (:start-server action-map))
    (swing/toggle-action (:configure action-map))
    (swing/toggle-action (:load action-map))))

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
  (.setEnabled (:save (session/action-map session)) (session/is-loaded? session)))

(defn action-map-fn
  "Designed to be called as a partial to bake in the session"
  [session]
  {:start-server (swing/make-safe-action session
                                   "Start Server" (partial start-server session) true)
   :stop-server (swing/make-safe-action session
                                  "Stop Server" (partial stop-server session) false)
   :configure (swing/make-safe-action session
                                "Configure" (partial config/configure session) true)
   :exit (swing/make-safe-action session "Exit" exit true)
   :load (swing/make-safe-action session "Load Session..."
                           #(wrap-session-action session
                                                 (partial session/load-session! session)
                                                 %1) true)
   :save (swing/make-safe-action session
                           "Save Session" (partial session/save-session session) false)
   :save-as (swing/make-safe-action session "Save Session As..."
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
                             (swing/log-action "resend-request")
                             (resend-all-requests session rows))
    (setEnabled [enabled] (proxy-super setEnabled
                                       (and enabled
                                            (server/running?
                                             (state/current-server session)))))))

(defn- save-body-action [session key table]
  (proxy [com.nwalex.sponge.gui.BodyPanel$SaveAction] [table]
    (saveText [text row]
              (swing/log-action (format "save-body %s" key))
              (model/update-exchange-body! session text key row))))

(defn- resend-request-action [session table]
  (make-table-action session table :resend-request
                     (partial resend-request-proxy session)))

(defn- update-body-action [session table key action-key]
  (make-table-action session table action-key (partial save-body-action session key)))

(defn- make-sponge-session-controller
  [session action-map plugin-controller]
  (proxy [com.nwalex.sponge.gui.SpongeSessionController] []
    (setName [name] (session/set-name! session name))
    (updateSessionInfo [] (state/set-session-info session))
    (getPluginController [] plugin-controller)
    (getStartServerAction [] (:start-server action-map))
    (getStopServerAction [] (:stop-server action-map))
    (getConfigureAction [] (:configure action-map))
    (getExitAction [] (:exit action-map))
    (getExchangeTableModel [] (session/table-model session))    
    (getRequestDataForRow [row] (model/get-data-for-row session
                                                        row :request))
    (getResponseDataForRow [row] (model/get-data-for-row session
                                                         row :response))       
    (getLabelExchangeAction [table] (swing/make-multi-row-action
                                     (partial label/do-label session) table))
    (getDeleteLabelAction [table] (swing/make-multi-row-action
                                      (partial label/delete-label session) table))
    (getLoadAction [] (:load action-map))
    (getSaveAction [] (:save-as action-map))
    (getSaveAsAction [] (:save-as action-map))
    (getSetDefaultResponseAction [table]
                                 (swing/make-multi-row-action
                                  (partial model/set-as-default-response! session)
                                  table))
    (getMode [] (state/get-mode session))
    (setMode [mode] (filters/set-mode session mode))
    (getDeleteRowAction [table] (swing/make-multi-row-action
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
    (getDuplicateRowAction [table] (swing/make-multi-row-action
                                    (partial model/duplicate-rows! session)
                                    table))))


(defn- get-property [props name default]
  (if props    
    (.getProperty props name default)
    default))

(defn create-session [workspace]
  (log/info "Creating session...")
  (let [session (session/make-session workspace)
        action-map (action-map-fn session)
        plugin-controller (filters/make-plugin-controller session)
        session-controller (make-sponge-session-controller
                        session action-map plugin-controller)]
    (session/init-gui! session session-controller action-map)
    (session/init-table-model! session
                               (model/make-exchange-table-model session))
    ;; set the default config
    (state/set-config! session
                       (get-property @(:config-props workspace)
                                     "sponge.default.port"
                                     "8139")                     
                       (get-property @(:config-props workspace)
                                     "sponge.default.target"
                                     "http://services.aonaware.com"))        
    session))
