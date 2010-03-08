(ns com.nwalex.sponge.session
  (:require
   [clojure.contrib.logging :as log]))

(defn load-session [event]
  (log/info "Ready to load session"))

(defn save-session [event]
  (log/info "Ready to save session"))

(defn save-session-as [event]
  (log/info "Ready to save session as"))
