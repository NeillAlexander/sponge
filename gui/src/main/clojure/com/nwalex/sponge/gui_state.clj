(ns com.nwalex.sponge.gui-state
  (:require
   [clojure.contrib.swing-utils :as swing]))

;; the currently running server
(def #^{:private true} current-server-store (atom nil))
(def #^{:private true} gui-frame-store (atom nil))
(def #^{:private true} port-store (ref 8139))
(def #^{:private true} target-store (ref "http://localhost:8141"))
(def #^{:private true} exchange-store (atom []))

(def #^{:private true} id-store
     (atom (java.util.concurrent.atomic.AtomicLong.)))

(defn- set-new-atom [old new]
  new)

(defn get-value-at [row col]
  ((@exchange-store row) (if (= 0 col) :type :body)))

(def exchange-table-model
     (proxy [javax.swing.table.AbstractTableModel] []
       (getColumnCount [] 2)
       (getColumnName [i] (if (= i 0) "Type" "Body"))
       (getRowCount [] (count @exchange-store))
       (getValueAt [row col] (get-value-at row col))))

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

(defn- assign-id-to [exchange]
  (if (:id exchange)
    exchange
    (assoc exchange :id (.getAndIncrement @id-store))))

(defn add-exchange! [server exchange key]
  (let [exchange-with-id (assign-id-to exchange)]
    (swap! exchange-store conj
           {:type (if (= :request key) "Request" "Response")
            :body (:body (key exchange))})
    (swing/do-swing
     (.fireTableDataChanged exchange-table-model))
    exchange-with-id))
