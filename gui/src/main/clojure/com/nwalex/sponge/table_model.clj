(ns com.nwalex.sponge.table-model
  (:require
   [com.nwalex.sponge.soap :as soap]
   [com.nwalex.sponge.exchange :as exchange]
   [clojure.contrib.swing-utils :as swing]
   [clojure.contrib.logging :as log]))

(declare get-value-at notify-data-changed make-default-response-key)

(def #^{:private true} default-responses (ref {}))
(def #^{:private true} data-id-store (ref []))

(def #^{:private true} columns ["Status" "Namespace" "URL" "Soap Method"
                                "Start" "End" "Time (ms)" "Info"
                                "Replays" "Label"])

(def #^{:private true} exchange-table-model
     (proxy [javax.swing.table.AbstractTableModel] []
       (getColumnCount [] (count columns))
       (getColumnName [i] (columns i))
       (getRowCount [] (count @data-id-store))
       (getValueAt [row col] (get-value-at row col))))

(defn get-persistence-map []
  {:data-id-store @data-id-store
   :default-responses @default-responses})

(defn load-from-persistence-map [persistence-map]  
  (dosync   
   (ref-set data-id-store (:data-id-store persistence-map))
   (ref-set default-responses (:default-responses persistence-map)))
  (notify-data-changed))

(defn- calc-status-from [data]
  (if (:response data) (:status (:response data)) "Requesting..."))

(defn- format-date [time-ms]
  (if time-ms
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss.SSS")
             (java.util.Date. time-ms))
    "n/a"))


(defn- get-exchange-for-row [row]
  (exchange/get-exchange (@data-id-store row)))

(defn get-value-at [row col]
  ;; see the columns vector above
  (let [exchange (get-exchange-for-row row)]    
    (cond
     (= col 0) (calc-status-from exchange)     
     (= col 1) (:namespace exchange)
     (= col 2) (:uri (:request exchange))
     (= col 3) (:soap-method exchange)
     (= col 4) (format-date (:started exchange))
     (= col 5) (format-date (:ended exchange))
     (= col 6) (if (:ended exchange)
                 (- (:ended exchange) (:started exchange))
                 "n/a")
     (= col 7) (if (= (:id exchange)
                      (@default-responses
                        (make-default-response-key exchange)))
                 "R"
                 "")
     (= col 8) (:num-replays exchange)
     (= col 9) (if (:label exchange) (:label exchange) ""))))

(defn get-data-for-row
  "Returns the full xml body for the row"
  [row key]
  (exchange/get-pretty-printed-body (get-exchange-for-row row) key))

(defn- add-entry
  [exchange]
  ;; only add the id once (on request)
  (dosync 
   (if (not (exchange/known? exchange))
     (commute data-id-store conj (:id exchange)))
   ;; but always update the data store
   (exchange/save exchange)))

(defn- notify-data-changed []
  (swing/do-swing
     (.fireTableDataChanged exchange-table-model)))

(defn- notify-row-changed [row]
  (swing/do-swing
   (.fireTableRowsUpdated exchange-table-model 0
                          (dec (count @data-id-store)))))

(defn- notify-row-added [row]
  (swing/do-swing
   (.fireTableRowsInserted exchange-table-model row row)))

(defn- notify-row-deleted [row]
  (swing/do-swing
   (.fireTableRowsDeleted exchange-table-model row row)))

(defn add-exchange!
  "Add the exchange represented by map m"
  [server m key]
  (let [exchange (exchange/init m key)]
    (add-entry exchange)
    (notify-row-added (dec (count @data-id-store)))
    exchange))

(defn get-table-model []
  exchange-table-model)

(defn clear [event]
  (dosync
   (ref-set data-id-store [])
   (exchange/delete-all))
  (notify-data-changed))

(defn set-label-on-row [label row]
  (let [exchange (get-exchange-for-row row)]
    (exchange/set-label exchange label)    
    (notify-row-changed row)))

(defn delete-current-row [row]
  (let [exchange (get-exchange-for-row row)]
    (dosync
     (exchange/delete exchange)
     (ref-set data-id-store
              (vec (concat (subvec @data-id-store 0 row)
                           (subvec @data-id-store (inc row)))))
     )
    (notify-row-deleted row)))

(defn get-label-for-row [row]
  (let [exchange (get-exchange-for-row row)]
    (if (:label exchange) (:label exchange) "")))

(defn delete-label-on-row [row]
  (set-label-on-row nil row))

(defn- make-default-response-key
  ([exchange]
     (if (:namespace exchange)
       (make-default-response-key
                         (:namespace exchange)
                         (:uri (:request exchange))
                         (:soap-method exchange))
       (make-default-response-key (:namespace (soap/parse-soap (:request exchange)))
                                  (:uri (:request exchange))
                                  (:soap-method (soap/parse-soap (:request exchange))))))
  ([namespace uri soap-method]
     (str namespace "-" uri "-" soap-method)))

(defn use-current-row-response [row]  
  (let [exchange (get-exchange-for-row row)
        key (make-default-response-key exchange)]
    ;; if already set then unset (toggle)
    (if (= (@default-responses key) (:id exchange))
      (dosync
       (commute default-responses dissoc key))
      (dosync
       (commute default-responses assoc key (:id exchange))))
    (notify-row-changed row)))

(defn replay-response [exchange]
  (let [response-id (@default-responses (make-default-response-key exchange))]
    (if response-id
      (let [exchange (exchange/get-exchange response-id)]
        (exchange/inc-replays exchange)
        (notify-data-changed)
        (:response exchange)))))
