(ns com.nwalex.sponge.session
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]
   [clojure.contrib.logging :as log]))

(defn- choose-file []
  (let [file-chooser (doto (javax.swing.JFileChooser.)
                       (.setApproveButtonText "Save"))
        response (.showOpenDialog file-chooser (state/gui))]
    (if (= response javax.swing.JFileChooser/APPROVE_OPTION)
      (.getSelectedFile file-chooser))))

(defn load-session [event]
  (log/info "Ready to load session"))

(defn save-session [event]
  (log/info "Ready to save session"))

(defn save-session-as [event]
  (let [file (choose-file)]
    (if file
      (do
        (log/info (format "Ready to save in file %s" file)))
      (log/info "No file chosen"))))
