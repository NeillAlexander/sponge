; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.table-model
  (:require
   [com.nwalex.sponge.soap :as soap]
   [com.nwalex.sponge.exchange :as exchange]
   [com.nwalex.sponge.gui-state :as state]
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

(defn load-from-persistence-map! [persistence-map]  
  (dosync   
   (ref-set data-id-store (:data-id-store persistence-map))
   (ref-set default-responses (:default-responses persistence-map)))
  (notify-data-changed))

(defn- get-exchange-for-row [row]
  (exchange/get-exchange (@data-id-store row)))

(defn get-value-at [row col]
  ;; see the columns vector above
  (let [exchange (get-exchange-for-row row)]    
    (cond
     (= col 0) (exchange/get-status exchange)     
     (= col 1) (exchange/get-namespace exchange)
     (= col 2) (exchange/get-uri exchange)
     (= col 3) (exchange/get-soap-method exchange)
     (= col 4) (exchange/get-start-date exchange)
     (= col 5) (exchange/get-end-date exchange)
     (= col 6) (exchange/get-request-time exchange)
     (= col 7) (if (= (exchange/get-id exchange)
                      (@default-responses
                        (make-default-response-key exchange)))
                 "R"
                 "")
     (= col 8) (exchange/get-num-replays exchange)
     (= col 9) (exchange/get-label exchange))))

(defn get-data-for-row
  "Returns the full xml body for the row"
  [row key]
  (exchange/get-pretty-printed-body (get-exchange-for-row row) key))

(defn- add-entry!
  [exchange]
  ;; only add the id once (on request)
  (dosync 
   (if (not (exchange/known? exchange))
     (commute data-id-store conj (exchange/get-id exchange)))
   ;; but always update the data store
   (exchange/save! exchange)))

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
  (log/info (format "Notify row deleted: %d" row))
  (swing/do-swing
   (.fireTableRowsDeleted exchange-table-model row row)))

(defn add-exchange!
  "Add the exchange represented by map m"
  [server m key]
  (let [exchange (exchange/init m key)]
    (add-entry! exchange)
    (notify-row-added (dec (count @data-id-store)))
    exchange))

(defn get-table-model []
  exchange-table-model)

(defn clear! [event]
  (log/info "Clearing all saved exchanges...")
  (dosync
   (state/clear-current-row!)
   (ref-set data-id-store [])
   (exchange/delete-all!)
   (ref-set default-responses {}))
  (log/info "Finished clearing all saved exchanges")
  (notify-data-changed))

(defn set-label-on-row [label row]
  (let [exchange (get-exchange-for-row row)]
    (exchange/set-label! exchange label)    
    (notify-row-changed row)))

(defn- get-default-response
  "Return the default response for this exchange type (if any)"
  [exchange]
  (@default-responses (make-default-response-key exchange)))

(defn- delete-default-response-if-required!
  "Delete the default response if this is the response exchange"
  [exchange]
  (let [response-exchange (get-default-response exchange)]
    (if (= (exchange/get-id exchange/get-id)
           (exchange/get-id response-exchange))
      (log/info (format "Deleting default response id = %s"
                        (exchange/get-id exchange)))
      (commute default-responses
               dissoc (make-default-response-key exchange)))))

(defn delete-current-row! [row]
  (log/info (format "Deleting row %d" row))
  (log/info (format "Num rows before delete = %d" (count @data-id-store)))
  (log/info (format "Num exchanges before delete = %d"
                    (exchange/get-num-exchanges)))
  (let [exchange (get-exchange-for-row row)]
    (dosync
     (exchange/delete! exchange)
     (ref-set data-id-store
              (vec (concat (subvec @data-id-store 0 row)
                           (subvec @data-id-store (inc row)))))
     (delete-default-response-if-required! exchange))
    (log/info (format "Num rows after delete = %d" (count @data-id-store)))
    (notify-row-deleted row)))

(defn get-label-for-row [row]
  (let [exchange (get-exchange-for-row row)]
    (if (:label exchange) (:label exchange) "")))

(defn delete-label-on-row [row]
  (set-label-on-row nil row))

(defn- make-default-response-key
  ([exchange]
     (if (exchange/get-namespace exchange)
       (make-default-response-key
                         (exchange/get-namespace exchange)
                         (exchange/get-uri exchange)
                         (exchange/get-soap-method exchange))
       (make-default-response-key (:namespace (soap/parse-soap (:request exchange)))
                                  (:uri (:request exchange))
                                  (:soap-method (soap/parse-soap (:request exchange))))))
  ([namespace uri soap-method]
     (str namespace "-" uri "-" soap-method)))

(defn use-current-row-response! [row]  
  (let [exchange (get-exchange-for-row row)
        key (make-default-response-key exchange)]
    ;; if already set then unset (toggle)
    (if (= (@default-responses key) (exchange/get-id exchange))
      (dosync
       (commute default-responses dissoc key))
      (dosync
       (commute default-responses assoc key (exchange/get-id exchange))))
    (notify-row-changed row)))

(defn replay-response [exchange]
  (let [response-id (get-default-response exchange)]
    (if response-id
      (let [exchange (exchange/get-exchange response-id)]
        (exchange/inc-replays! exchange)
        (notify-data-changed)
        (:response exchange)))))
