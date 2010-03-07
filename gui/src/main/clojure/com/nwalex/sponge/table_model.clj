(ns com.nwalex.sponge.table-model
  (:require
   [clojure.contrib.swing-utils :as swing]))

(declare get-value-at)

(def #^{:private true} exchange-store (atom []))
(def #^{:private true} id-store
     (atom (java.util.concurrent.atomic.AtomicLong.)))

(def #^{:private true} exchange-table-model
     (proxy [javax.swing.table.AbstractTableModel] []
       (getColumnCount [] 2)
       (getColumnName [i] (if (= i 0) "Type" "Body"))
       (getRowCount [] (count @exchange-store))
       (getValueAt [row col] (get-value-at row col))))

(defn get-value-at [row col]
  ((@exchange-store row) (if (= 0 col) :type :body)))

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

(defn get-table-model []
  exchange-table-model)
