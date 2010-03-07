(ns com.nwalex.sponge.table-model
  (:require
   [clojure.contrib.swing-utils :as swing]))

(declare get-value-at)

(def #^{:private true} exchange-store (atom []))
(def #^{:private true} table-data-store (atom {}))
(def #^{:private true} id-store
     (atom (java.util.concurrent.atomic.AtomicLong.)))

(def #^{:private true} exchange-table-model
     (proxy [javax.swing.table.AbstractTableModel] []
       (getColumnCount [] 2)
       (getColumnName [i] (if (= i 0) "Type" "Body"))
       (getRowCount [] (count @exchange-store))
       (getValueAt [row col] (get-value-at row col))))

(defn get-value-at [row col]
  (let [data (@table-data-store (@exchange-store row))]
    (if (= 0 col) "Exchange" "Other")))

(defn get-data-for-row [row key]
  (format "<data>Data for <row>row %d %s</row> here</data>" row key))

(defn- assign-id-to [exchange]
  (if (:id exchange)
    exchange
    (assoc exchange :id (.getAndIncrement @id-store))))

(defn- make-table-data
  "Create the map structure for all the data"
  [exchange key]  
  (let [data (if (@table-data-store (:id exchange))
               (@table-data-store (:id exchange))               
               {:id (:id exchange)})]
    (assoc data key {:body (key exchange)})))

(defn- add-entry
  [table-data]
  (swap! exchange-store conj (:id table-data))
  (swap! table-data-store assoc :id table-data))

(defn add-exchange! [server exchange key]
  (let [exchange-with-id (assign-id-to exchange)]
    (add-entry (make-table-data exchange-with-id key))
    (swing/do-swing
     (.fireTableDataChanged exchange-table-model))
    exchange-with-id))

(defn get-table-model []
  exchange-table-model)
