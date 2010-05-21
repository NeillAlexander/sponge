; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.session
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]
   [com.nwalex.sponge.exchange :as exchange]
   [com.nwalex.sponge.persistence :as persistence]
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as io]))

;;----------------------------------------------------
;; This is central to the multi-session Sponge
;; TODO: refactor into multi-layered map instead of flat
;;----------------------------------------------------

(defn make-session
  "Create the session data structure"
  [workspace]
  {:dirty (ref false)
   :name (ref "New session")
   :persistence-cookie (persistence/make-cookie workspace "Sponge Data Files" "spd")
   :workspace workspace
   :gui-controller (ref nil)
   :action-map (ref nil)
   :default-responses (ref {})
   :exchange-table-model (ref nil)
   :plugin-controller (ref nil)
   :plugin-manager (ref nil)
   :request-plugins (ref {})
   :response-plugins (ref {})
   :data-id-store (ref [])
   :active-data-id-store (ref [])
   :exchange-store (ref {})
   :next-exchange-id (ref (java.util.concurrent.atomic.AtomicLong.))
   :replay-count (ref {})   
   :current-server-store (atom nil)   
   :port-store (ref 8139)
   :target-store (ref "http://services.aonaware.com")
   :mode (atom com.nwalex.sponge.gui.SpongeSessionController/REPLAY_OR_FORWARD)})

;;----------------------------------------------------

(defn init-gui! [session gui-controller action-map]
  (dosync
   (ref-set (:gui-controller session) gui-controller)
   (ref-set (:action-map session) action-map)))

(defn init-table-model! [session model]
  (dosync
   (ref-set (:exchange-table-model session) model)))

(defn table-model [session]
  @(:exchange-table-model session))

(defn gui-controller [session]
  @(:gui-controller session))

(defn action-map [session]
  @(:action-map session))

(defn is-loaded? [session]
  (persistence/has-file? (:persistence-cookie session)))

(defn set-name! [session name]
  (let [current-file (persistence/current-file (:persistence-cookie session))]
    (log/info (format "ready to set name: '%s' or '%s'" name current-file))
    (dosync (ref-set (:name session)
                     (if name
                       name
                       (if current-file
                         (.getName current-file)
                         "untitled"))))))

(defn load-data! [session persistence-map]
  (set-name! session (:name persistence-map))
  (state/load-from-persistence-map! session (:gui-state persistence-map))
  (model/load-from-persistence-map! session (:table-model persistence-map))
  (exchange/load-from-persistence-map! session (:exchange persistence-map)))

(defn persistence-data [session]
  (assoc {}
    :gui-state (state/get-persistence-map session)
    :table-model (model/get-persistence-map session)
    :exchange (exchange/get-persistence-map session)
    :name @(:name session)))

(defn load-session-from-file! [session file]
  (log/info "Ready to load session from file")
  (persistence/load-data (:persistence-cookie session)
                         (partial load-data! session)
                         file))

(defn load-session! [session event]
  (log/info "Ready to load session...")
  (persistence/load-data (:persistence-cookie session) (partial load-data! session)))

(defn save-session [session event]
  (log/info "Loading session...")
  (persistence/save-data (:persistence-cookie session) (persistence-data session)))

(defn save-session-as [session event]
  (log/info "Saving session as...")
  (persistence/save-data-as (:persistence-cookie session) (persistence-data session)))
