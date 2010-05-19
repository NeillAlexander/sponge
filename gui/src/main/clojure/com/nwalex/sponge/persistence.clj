; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.persistence
  (:require
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as io]))

(defn make-cookie [workspace]
  {:directory (ref (System/getProperty "sponge.sessions"))
   :current-file (ref nil)
   :workspace workspace})

(defn- cookie-value [cookie key]
  @(cookie key))

(defn has-file? [cookie]
  (not (nil? (cookie-value cookie :current-file))))

(defn- update-cookie [cookie key value]
  (dosync
   (ref-set (cookie key) value)))

(defn update-current-file! [cookie file]
  (update-cookie cookie :current-file file))

(defn update-directory! [cookie dir]
  (update-cookie cookie :directory dir))

(defn- init-file-chooser [cookie text]
  (let [file-chooser (doto (javax.swing.JFileChooser.)
    (.setApproveButtonText text)
    (.setCurrentDirectory
     (java.io.File. (cookie-value cookie :directory))))]
    (if (has-file? cookie)
      (.setSelectedFile file-chooser (cookie-value cookie :current-file)))
    file-chooser))

(defn- gui [cookie]
  @(:gui-frame-store (:workspace cookie)))

(defn- choose-file
  "Launches JFileChooser. Remembers directory of chosen file for next time"
  [cookie text]
  (log/info (format "Prompting to choose a file for mode %s" text))
  (let [file-chooser (init-file-chooser cookie text)
        response (.showOpenDialog file-chooser (gui cookie))]
    (if (= response javax.swing.JFileChooser/APPROVE_OPTION)
      (do
        (update-directory! cookie (.getParent (.getSelectedFile file-chooser)))
        (.getSelectedFile file-chooser)))))


(defn save-data [cookie data]
  (let [file (cookie-value cookie :current-file)]
    (if file
      (do
        (log/info (format "Ready to save in file %s" file))
        (with-open [out (io/writer (java.io.BufferedOutputStream.
                                    (java.util.zip.GZIPOutputStream.
                                     (java.io.FileOutputStream. file))))]
          (.write out (str data))
          (.write out "\n"))
        (update-current-file! cookie file)
        (log/info "Done"))      
      (log/info "No file chosen"))))

(defn save-data-as [cookie data]
  (let [file (choose-file cookie "Save")]
    (update-cookie cookie :current-file file)
    (save-data cookie data)))

(defn- load-data-from-file [cookie file]
  (log/info (format "Loading data from file: %s" file))
  (with-open [in (java.io.PushbackReader.
                      (io/reader (java.util.zip.GZIPInputStream.
                                  (java.io.FileInputStream. file))))]
    (read in)))

(defn load-data
  ([cookie loader-fn file]
     (if file
         (let [data (load-data-from-file cookie file)]
           (if data
             (do
               (loader-fn data)
               (update-cookie cookie :current-file file))
             (log/info "No data loaded")))
         (log/info "No file chosen")))
  ([cookie loader-fn]
     (log/info "Ready to load data...")
     (load-data cookie loader-fn (choose-file cookie "Load"))))
