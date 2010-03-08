(ns com.nwalex.sponge.gui-state
  (:require
   [clojure.contrib.logging :as log]))

;; the currently running server
(def #^{:private true} current-server-store (atom nil))
(def #^{:private true} gui-frame-store (atom nil))
(def #^{:private true} port-store (ref 8139))
(def #^{:private true} target-store (ref "http://services.aonaware.com"))
(def #^{:private true} current-row-store (atom nil))

(defn- set-new-atom [old new]
  new)

(defn set-gui! [gui]
  (swap! gui-frame-store set-new-atom gui))

(defn gui [] @gui-frame-store)

(defn set-current-server! [server]
  (swap! current-server-store set-new-atom server))

(defn current-server []
  @current-server-store)

(defn set-config! [port target]
  (dosync
   (commute port-store set-new-atom port)
   (commute target-store set-new-atom target)))

(defn config []
  {:port @port-store :target @target-store})

(defn current-row []
  @current-row-store)

(defn set-current-row! [row]
  (swap! current-row-store set-new-atom (if (> row -1) row nil))
  (log/debug (format "Current selected row = %s" (current-row))))

(defn row-selected []
  (not (nil? (current-row))))
