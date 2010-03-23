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

(def #^{:private true} session-file (atom nil))
(def #^{:private true} sponge-sessions-dir
     (atom (System/getProperty "sponge.sessions")))

(defn has-file []
  (not (nil? @session-file)))

(defn- choose-file
  "Launches JFileChooser. Remembers directory of chosen file for next time"
  [text]
  (let [file-chooser (doto (javax.swing.JFileChooser.)
                       (.setApproveButtonText text)
                       (.setCurrentDirectory
                        (java.io.File. @sponge-sessions-dir)))
        response (.showOpenDialog file-chooser (state/gui))]
    (if (= response javax.swing.JFileChooser/APPROVE_OPTION)
      (do
        (compare-and-set! sponge-sessions-dir @sponge-sessions-dir
                          (.getParent (.getSelectedFile file-chooser)))
        (.getSelectedFile file-chooser)))))

(defn load-session! [event]
  (let [file (choose-file "Load")]
    (if file
      (with-open [in (java.io.PushbackReader.
                      (io/reader (java.util.zip.GZIPInputStream.
                                  (java.io.FileInputStream. file))))]
        (let [persistence-map (read in)]          
          (state/load-from-persistence-map! (:gui-state persistence-map))
          (model/load-from-persistence-map! (:table-model persistence-map))
          (exchange/load-from-persistence-map! (:exchange persistence-map))
          (compare-and-set! session-file @session-file file)))      
      (log/info "No file chosen"))))

(defn- save-session-to-file [file]
  (if file
    (let [persistence-map (assoc {}
                            :gui-state (state/get-persistence-map)
                            :table-model (model/get-persistence-map)
                            :exchange (exchange/get-persistence-map))]
      (log/info (format "Ready to save in file %s" file))
      (with-open [out (io/writer (java.io.BufferedOutputStream.
                                  (java.util.zip.GZIPOutputStream.
                                   (java.io.FileOutputStream. file))))]
        (.write out (str persistence-map))
        (.write out "\n"))      
      (compare-and-set! session-file @session-file file)
      (log/info "Done"))      
    (log/info "No file chosen")))

(defn save-session [event]
  (save-session-to-file @session-file))

(defn save-session-as [event]
  (let [file (choose-file "Save")]
    (save-session-to-file file)))
