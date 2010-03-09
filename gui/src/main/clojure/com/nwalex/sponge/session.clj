(ns com.nwalex.sponge.session
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as io]))

(def #^{:private true} session-file (atom nil))

(defn has-file []
  (not (nil? @session-file)))

(defn- choose-file [text]
  (let [file-chooser (doto (javax.swing.JFileChooser.)
                       (.setApproveButtonText text))
        response (.showOpenDialog file-chooser (state/gui))]
    (if (= response javax.swing.JFileChooser/APPROVE_OPTION)
      (.getSelectedFile file-chooser))))

(defn load-session [event]
  (let [file (choose-file "Load")]
    (if file
      (with-open [in (java.io.PushbackReader.
                      (io/reader (java.util.zip.GZIPInputStream.
                                  (java.io.FileInputStream. file))))]
        (let [persistence-map (read in)]          
          (state/load-from-persistence-map (:gui-state persistence-map))
          (model/load-from-persistence-map (:table-model persistence-map))
          (compare-and-set! session-file @session-file file)))      
      (log/info "No file chosen"))))

(defn- save-session-to-file [file]
  (if file
    (let [persistence-map (assoc {}
                            :gui-state (state/get-persistence-map)
                            :table-model (model/get-persistence-map))]
      (log/info (format "Ready to save in file %s" file))
      (with-open [out (io/writer (java.io.BufferedOutputStream.
                                  (java.util.zip.GZIPOutputStream.
                                   (java.io.FileOutputStream. file))))]
        (.write out (str persistence-map)))      
      (compare-and-set! session-file @session-file file)
      (log/info "Done"))      
    (log/info "No file chosen")))

(defn save-session [event]
  (save-session-to-file @session-file))

(defn save-session-as [event]
  (let [file (choose-file "Save")]
    (save-session-to-file file)))
