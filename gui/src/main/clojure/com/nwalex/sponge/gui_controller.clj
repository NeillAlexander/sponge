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
   [com.nwalex.sponge.exchange :as exchange]))

(declare action-map)

(defn- make-action [name f enabled]
  (doto (proxy [javax.swing.AbstractAction] [name]
          (actionPerformed [event] (f event)))
    (.setEnabled enabled)))

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
  (future   
   (core/start-repl 4006)))

(defn- exit [event]
  (System/exit 1))

(defn- delete-row [event]
  (model/delete-current-row! (state/current-row)))

(defn- resend-request [event]
  (let [exchange (model/get-exchange-for-row (state/current-row))]
    (future (server/resend-request exchange (state/current-server)))))

(defn- update-row [row]
  (state/set-current-row! row)
  (.setEnabled (:label-action action-map) (state/row-selected))
  (.setEnabled (:delete-label action-map) (state/row-selected))
  (.setEnabled (:use-response action-map) (state/row-selected))
  (.setEnabled (:delete-row action-map) (state/row-selected))
  (.setEnabled (:resend-request action-map)
               (and (state/row-selected) (server/running? (state/current-server)))))

(defn- wrap-session-action [f event]
  (f event)
  (.setEnabled (:save action-map) (session/has-file)))

(defn- use-response [event]
  (model/use-current-row-response! (state/current-row)))

(def action-map
     {:start-server (make-action "Start Server" start-server true)
      :stop-server (make-action "Stop Server" stop-server false)
      :configure (make-action "Configure" config/configure true)
      :exit (make-action "Exit" exit true)
      :start-repl (make-action "Start Repl" start-repl true)
      :clear-all (make-action "Clear All" model/clear! true)
      :label-action (make-action "Attach Label..." label/do-label false)
      :delete-label (make-action "Delete Label" label/delete-label false)
      :load (make-action "Load Session..."
                         #(wrap-session-action session/load-session! %1) true)
      :save (make-action "Save Session" session/save-session false)
      :save-as (make-action "Save Session As..."
                            #(wrap-session-action session/save-session-as %1)
                            true)
      :use-response (make-action "Use this Response" use-response false)
      :delete-row (make-action "Delete Exchange" delete-row false)
      :resend-request (make-action "Resend this Request" resend-request false)})

(defn- set-mode [mode]  
  (state/set-mode! mode)
  (if (server/running? (state/current-server))
    (do
      (stop-server nil)
      (start-server nil))))


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
       (getDeleteLabelAction [] (:delete-label action-map))
       (getLoadAction [] (:load action-map))
       (getSaveAction [] (:save action-map))
       (getSaveAsAction [] (:save-as action-map))
       (getSetDefaultResponseAction [] (:use-response action-map))
       (getMode [] (state/get-mode))
       (setMode [mode] (set-mode mode))
       (getDeleteRowAction [] (:delete-row action-map))
       (getResendRequestAction [] (:resend-request action-map))))

(defn make-gui [& args]
  (state/set-gui! (doto (com.nwalex.sponge.gui.SpongeGUI. sponge-controller)
           (.setVisible true))))
