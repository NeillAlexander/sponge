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
   [clojure.contrib.swing-utils :as swing]))

(declare action-map)

(defn- make-action [name f enabled]
  (doto (proxy [javax.swing.AbstractAction] [name]
          (actionPerformed [event] (f event)))
    (.setEnabled enabled)))

(defn- make-safe-action [name f enabled]
  (com.nwalex.sponge.gui.SafeAction.
   (make-action name f enabled)
   (state/gui)))

(defn- make-multi-row-action [f table]
  (proxy [com.nwalex.sponge.gui.JXTableMultiRowAction] [table]
    (multiRowActionPerformed [indices] (f indices))))

(defn- make-single-row-action [f table]
  (proxy [com.nwalex.sponge.gui.JXTableSingleRowAction] [table]
    (singleRowActionPerformed [row] (f row))))

(defn- toggle-action [action]
  (.setEnabled action (not (.isEnabled action))))

(defn- toggle-started []
  (toggle-action (:stop-server action-map))
  (toggle-action (:start-server action-map))
  (toggle-action (:configure action-map))
  (toggle-action (:load action-map)))

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
                                 :request-filters (filters/get-request-filters-for-mode
                                                   (state/get-mode))
                                 :response-filters (filters/get-response-filters-for-mode
                                                    (state/get-mode))))))
  (toggle-started))

(defn- start-repl [event]
  (toggle-action (:start-repl action-map))
  (state/repl-started!)
  (future   
   (core/start-repl 4006)))

(defn- exit [event]
  (System/exit 1))

(defn- resend-request [row]
  (let [exchange (model/get-exchange-for-row row)]
    (future (server/resend-request exchange (state/current-server))))
  0)

(defn- resend-all-requests [rows]
  (amap rows idx ret (resend-request (aget rows idx))))

(defn- wrap-session-action [f event]
  (f event)
  (.setEnabled (:save action-map) (session/has-file)))

(def action-map
     {:start-server (make-safe-action "Start Server" start-server true)
      :stop-server (make-safe-action "Stop Server" stop-server false)
      :configure (make-safe-action "Configure" config/configure true)
      :exit (make-safe-action "Exit" exit true)
      :start-repl (make-safe-action "Start Repl" start-repl true)
      :load (make-safe-action "Load Session..."
                         #(wrap-session-action session/load-session! %1) true)
      :save (make-safe-action "Save Session" session/save-session false)
      :save-as (make-safe-action "Save Session As..."
                            #(wrap-session-action session/save-session-as %1)
                            true)
      :resend-request (ref nil)
      :update-request (ref nil)
      :update-response (ref nil)})

(defn- set-mode [mode]  
  (state/set-mode! mode)
  (if (server/running? (state/current-server))
    (server/update-request-filters (state/current-server)
                                   (filters/get-request-filters-for-mode
                                    (state/get-mode)))))

(defn- make-table-action [table key proxy-maker]
  (if-not @(key action-map)    
    (dosync
     (ref-set (key action-map)
              (proxy-maker table))))
  @(key action-map))

(defn- resend-request-proxy [table]
  (proxy [com.nwalex.sponge.gui.JXTableMultiRowAction] [table]
    (multiRowActionPerformed [rows] (resend-all-requests rows))
    (setEnabled [enabled] (proxy-super setEnabled
                                       (and enabled
                                            (server/running?
                                             (state/current-server)))))))

(defn- save-body-action [key table]
  (proxy [com.nwalex.sponge.gui.BodyPanel$SaveAction] [table]
    (saveText [text row] (model/update-exchange-body! text key row))))

(defn- resend-request-action [table]
  (make-table-action table :resend-request resend-request-proxy))

(defn- update-body-action [table key action-key]
  (make-table-action table action-key (partial save-body-action key)))

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
       (getLabelExchangeAction [table] (make-multi-row-action label/do-label table))
       (getDeleteLabelAction [table] (make-multi-row-action label/delete-label table))
       (getLoadAction [] (:load action-map))
       (getSaveAction [] (:save action-map))
       (getSaveAsAction [] (:save-as action-map))
       (getSetDefaultResponseAction [table]
                                    (make-multi-row-action
                                     model/set-as-default-response! table))
       (getMode [] (state/get-mode))
       (setMode [mode] (set-mode mode))
       (getDeleteRowAction [table] (make-multi-row-action
                                    model/delete-rows! table))
       (getResendRequestAction [table] (resend-request-action table))
       (getUpdateRequestBodyAction [table] (update-body-action table
                                                               :request
                                                               :update-request))
       (getUpdateResponseBodyAction [table] (update-body-action table
                                                                :response
                                                                :update-response))
       (getDuplicateRowAction [table] (make-multi-row-action
                                       model/duplicate-rows! table))))

(defn make-gui [& args]
  (swing/do-swing
   (state/set-gui!
    (doto (com.nwalex.sponge.gui.SpongeGUI. sponge-controller)
                     (.setVisible true)))))
