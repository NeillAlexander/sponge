(ns com.nwalex.sponge.gui-state
  (:require
   [clojure.contrib.logging :as log]))

;; the currently running server
(def #^{:private true} current-server-store (atom nil))
(def #^{:private true} gui-frame-store (atom nil))
(def #^{:private true} port-store (ref 8139))
(def #^{:private true} target-store (ref "http://services.aonaware.com"))
(def #^{:private true} current-row-store (atom nil))

(defn get-persistence-map []
  {:port @port-store :target @target-store})

(defn set-config! [port target]
  (dosync
   (ref-set port-store port)
   (ref-set target-store target)))

(defn load-from-persistence-map [persistence-map]
  (set-config! (:port persistence-map) (:target persistence-map)))

(defn set-gui! [gui]
  (compare-and-set! gui-frame-store @gui-frame-store gui))

(defn gui [] @gui-frame-store)

(defn set-current-server! [server]
  (compare-and-set! current-server-store @current-server-store server))

(defn current-server []
  @current-server-store)

(defn config []
  {:port @port-store :target @target-store})

(defn current-row []
  @current-row-store)

(defn set-current-row! [row]
  (compare-and-set! current-row-store
                    @current-row-store (if (> row -1) row nil))
  (log/debug (format "Current selected row = %s" (current-row))))

(defn row-selected []
  (not (nil? (current-row))))
