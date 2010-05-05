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

(defn get-exchange-for-row [row]
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

(defn- do-notification
  "Simple function to ensure the notification is always called start < end"
  [notification-fn a b]
  (let [start (if (< a b) a b)
        end (if (> b a) b a)]
    (log/info (format "Doing notification on range: %d %d" start end))
    (swing/do-swing
     (notification-fn start end))))

(defn- notify-row-changed
  ([row]
     (notify-row-changed row row))
  ([start end]
     (log/info (format "notify-row-changed %d %d" start end))
     (do-notification #(.fireTableRowsUpdated exchange-table-model %1 %2) start end)))

(defn- notify-row-added
  ([row]
     (notify-row-added row row))
  ([start end]
     (do-notification #(.fireTableRowsInserted exchange-table-model %1 %2) start end)))

(defn- notify-row-deleted
  ([row]
     (log/info (format "Notify row deleted: %d" row))
     (notify-row-deleted row row))
  ([start end]
     (log/info (format "Notify rows deleted: %d to %d" start end))
     (do-notification #(.fireTableRowsDeleted exchange-table-model %1 %2) start end)))

(defn add-exchange!
  "Add the exchange represented by map m"
  [server m key]
  (let [exchange (exchange/init m key)
        known? (exchange/known? exchange)]    
    (add-entry! exchange)
    (if known?
      (notify-row-changed 0 (dec (count @data-id-store)))
      (notify-row-added (dec (count @data-id-store))))
    exchange))

(defn get-table-model []
  exchange-table-model)

(defn- set-label-on-row [label row]
  (log/info (format "Setting label on row %d to %s" row label))
  (let [exchange (get-exchange-for-row row)]
    (exchange/set-label! exchange label)
    0))

(defn set-label-on-rows! [label rows]
  (amap rows idx ret (set-label-on-row label (aget rows idx)))
  (notify-row-changed (aget rows 0) (aget rows (dec (alength rows)))))

(defn update-exchange-body!
  "Updates the body on the currently selected exchange"
  [text key row]
  (log/info (format "Updating %s body for row %d" key row))
  (let [exchange (get-exchange-for-row row)]
    (exchange/update-body! exchange key text)
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

(defn- row-loop [row-indices row-fn notify-fn]
  (let [rows (reverse (sort row-indices))]
    (loop [idx (first rows)
           rem (rest rows)]
      (if idx
        (do
          (row-fn idx)
          (recur (first rem) (rest rem)))))
    (if notify-fn
      (notify-fn (last rows) (first rows)))))

(defn- duplicate-row!
  [row]
  (let [exchange (get-exchange-for-row row)
        duplicate (exchange/duplicate exchange)]
    (log/info (format "Duplicating row: %d" row))
    (dosync
     (ref-set data-id-store
              (vec (concat (subvec @data-id-store 0 row)
                           [(exchange/get-id duplicate)]
                           (subvec @data-id-store row))))
     (notify-row-added (inc row))))
  0)

(defn duplicate-rows! [rows]
  (row-loop rows duplicate-row! nil))

(defn- delete-row! [row]
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
    (log/info (format "Num rows after delete = %d" (count @data-id-store)))))

(defn delete-rows! [rows]
  (log/info (format "Delete rows: %s" rows))
  (row-loop rows delete-row! notify-row-deleted))

(defn get-label-for-row [row]
  (let [exchange (get-exchange-for-row row)]
    (if (:label exchange) (:label exchange) "")))

(defn delete-label-on-rows! [rows]  
  (set-label-on-rows! nil rows))

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

(defn- set-default-response! [row]  
  (let [exchange (get-exchange-for-row row)
        key (make-default-response-key exchange)]
    ;; if already set then unset (toggle)
    (if (= (@default-responses key) (exchange/get-id exchange))
      (dosync
       (commute default-responses dissoc key)
       (log/info (format "Unset default response for row %d" row)))
      (dosync
       (commute default-responses assoc key (exchange/get-id exchange))
       (log/info (format "Set row %d as default response" row))))
    0))

(defn set-as-default-response! [rows]
  (amap rows idx ret (set-default-response! (aget rows idx)))
  (notify-row-changed 0 (dec (count @data-id-store))))

(defn replay-response [exchange]
  (let [response-id (get-default-response exchange)]
    (if response-id
      (let [exchange (exchange/get-exchange response-id)]
        (exchange/inc-replays! exchange)
        (notify-row-changed 0 (dec (count @data-id-store)))
        (:response exchange)))))
