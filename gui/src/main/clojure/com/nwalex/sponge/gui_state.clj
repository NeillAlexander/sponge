(ns com.nwalex.sponge.gui-state)

;; the currently running server
(def #^{:private true} current-server-store (atom nil))
(def #^{:private true} gui-frame-store (atom nil))
(def #^{:private true} port-store (ref 8139))
(def #^{:private true} target-store (ref "http://services.aonaware.com"))

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
