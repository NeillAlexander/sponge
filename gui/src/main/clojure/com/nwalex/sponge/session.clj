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
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as io]))

;;----------------------------------------------------
;; This is central to the multi-session Sponge
;; TODO: refactor into multi-layered map instead of flat
;;----------------------------------------------------

(defn make-session
  "Create the session data structure"
  []
  {:file (ref nil)
   :gui-controller (ref nil)
   :action-map (ref nil)
   :sessions-dir (ref (System/getProperty "sponge.sessions"))
   :default-responses (ref {})
   :exchange-table-model (ref nil)
   :plugin-controller (ref nil)
   :plugin-manager (ref nil)
   :request-plugins (ref {})
   :response-plugins (ref {})
   :data-id-store (ref [])
   :active-data-id-store (ref [])
   :exchange-store (ref {})})

;;----------------------------------------------------

(defn get-session-file [session]
  @(:file session))

(defn has-file [session]
  (not (nil? (get-session-file session))))

(defn- update-session-file! [session file]
  (dosync
   (ref-set (:file session) file)))

(defn init-gui! [session gui-controller action-map]
  (dosync
   (ref-set (:gui-controller session) gui-controller)
   (ref-set (:action-map session) action-map)))

(defn init-table-model! [session model]
  (dosync
   (ref-set (:exchange-table-model session) model)))

(defn table-model [session]
  @(:exchange-table-model session))

(defn sessions-dir [session]
  @(:sessions-dir session))

(defn update-sessions-dir! [session dir]
  (dosync
   (ref-set (:sessions-dir session) dir)))

(defn gui-controller [session]
  @(:gui-controller session))

(defn action-map [session]
  @(:action-map session))

(defn- choose-file
  "Launches JFileChooser. Remembers directory of chosen file for next time"
  [session text]
  (let [file-chooser (doto (javax.swing.JFileChooser.)
                       (.setApproveButtonText text)
                       (.setCurrentDirectory
                        (java.io.File. (sessions-dir session))))
        response (.showOpenDialog file-chooser (state/gui))]
    (if (= response javax.swing.JFileChooser/APPROVE_OPTION)
      (do
        (update-sessions-dir! session (.getParent (.getSelectedFile file-chooser)))
        (.getSelectedFile file-chooser)))))

(defn load-session-from-file![session file]
  (log/info (format "Loading session from file: %s" file))
  (with-open [in (java.io.PushbackReader.
                      (io/reader (java.util.zip.GZIPInputStream.
                                  (java.io.FileInputStream. file))))]
        (let [persistence-map (read in)]          
          (state/load-from-persistence-map! (:gui-state persistence-map))
          (model/load-from-persistence-map! session (:table-model persistence-map))
          (exchange/load-from-persistence-map! session (:exchange persistence-map))
          (update-session-file! session file))))

(defn load-session! [session event]
  (let [file (choose-file session "Load")]
    (if file
      (load-session-from-file! session file)      
      (log/info "No file chosen"))))

(defn- save-session-to-file [session file]
  (if file
    (let [persistence-map (assoc {}
                            :gui-state (state/get-persistence-map)
                            :table-model (model/get-persistence-map session)
                            :exchange (exchange/get-persistence-map session))]
      (log/info (format "Ready to save in file %s" file))
      (with-open [out (io/writer (java.io.BufferedOutputStream.
                                  (java.util.zip.GZIPOutputStream.
                                   (java.io.FileOutputStream. file))))]
        (.write out (str persistence-map))
        (.write out "\n"))
      (update-session-file! session file)
      (log/info "Done"))      
    (log/info "No file chosen")))

(defn save-session [session event]
  (save-session-to-file session (get-session-file session)))

(defn save-session-as [session event]
  (let [file (choose-file session "Save")]
    (save-session-to-file session file)))
