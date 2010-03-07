(ns com.nwalex.sponge.table-model
  (:require
   [clojure.contrib.swing-utils :as swing]))

(declare get-value-at)

(def #^{:private true} data-id-store (atom []))
(def #^{:private true} table-data-store (atom {}))
(def #^{:private true} id-store
     (atom (java.util.concurrent.atomic.AtomicLong.)))

(def #^{:private true} columns ["Status" "Namespace" "URL" "Soap Method"
                                "Start" "End" "Elapsed Time (ms)"])

(def #^{:private true} exchange-table-model
     (proxy [javax.swing.table.AbstractTableModel] []
       (getColumnCount [] (count columns))
       (getColumnName [i] (columns i))
       (getRowCount [] (count @data-id-store))
       (getValueAt [row col] (get-value-at row col))))

(defn- calc-status-from [data]
  (if (:response data) "Done" "Requesting..."))

(defn- format-date [time-ms]
  (if time-ms
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss.SSS")
             (java.util.Date. time-ms))
    "n/a"))

(defn get-value-at [row col]
  (let [data (@table-data-store (@data-id-store row))]
    (cond
     (= col 0) (calc-status-from data)
     (= col 1) "Ns here"
     (= col 2) "URL here"
     (= col 3) "soapy"
     (= col 4) (format-date (:started data))
     (= col 5) (format-date (:ended data))
     (= col 6) (if (:ended data)
                 (- (:ended data) (:started data))
                 "n/a"))))

(defn get-data-for-row [row key]
  (let [data-id (@data-id-store row)]
    (:body (key (@table-data-store data-id)))))

(defn- assign-id-to [exchange]
  (if (:id exchange)
    exchange
    (assoc exchange :id (.getAndIncrement @id-store))))

(defn- make-table-data
  "Create the map structure for all the data"
  [exchange key]
  (let [data (if (@table-data-store (:id exchange))
               (@table-data-store (:id exchange))               
               {:id (:id exchange)})
        time-key (if (= :request key) :started :ended)]
    (assoc data
      key (key exchange)
      time-key (System/currentTimeMillis))))

(defn- not-already-saved [data]
  (not (@table-data-store (:id data))))

(defn- add-entry
  [table-data]
  ;; only add the id once (on request)
  (if (not-already-saved table-data)
    (swap! data-id-store conj (:id table-data)))
  ;; but always update the data store
  (swap! table-data-store assoc (:id table-data) table-data))

(defn add-exchange! [server exchange key]
  (let [exchange-with-id (assign-id-to exchange)]
    (add-entry (make-table-data exchange-with-id key))
    (swing/do-swing
     (.fireTableDataChanged exchange-table-model))
    exchange-with-id))

(defn get-table-model []
  exchange-table-model)
