(ns com.nwalex.sponge.session
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as io]))

(defn- choose-file [text]
  (let [file-chooser (doto (javax.swing.JFileChooser.)
                       (.setApproveButtonText text))
        response (.showOpenDialog file-chooser (state/gui))]
    (if (= response javax.swing.JFileChooser/APPROVE_OPTION)
      (.getSelectedFile file-chooser))))

(defn load-session [event]
  (let [file (choose-file "Load")]
    (if file
      (let [persistence-map {}]
        (log/info (format "Ready to load from file %s" file))
        ;(io/spit file (assoc persistence-map
        ;                :gui-state (state/get-persistence-map)
        ;                :table-model (model/get-persistence-map)))
        (log/info "Done"))      
      (log/info "No file chosen"))))

(defn save-session [event]
  (log/info "Ready to save session"))

(defn save-session-as [event]
  (let [file (choose-file "Save")]
    (if file
      (let [persistence-map {}]
        (log/info (format "Ready to save in file %s" file))
        (io/spit file (assoc persistence-map
                        :gui-state (state/get-persistence-map)
                        :table-model (model/get-persistence-map)))
        (log/info "Done"))      
      (log/info "No file chosen"))))
