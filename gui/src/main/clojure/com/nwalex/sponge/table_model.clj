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

;; this is a justified global because it is read only
(def #^{:private true} columns ["Status" "Namespace" "URL" "Soap Method"
                                "Start" "End" "Time (ms)" "Info"
                                "Replays" "Label"])

(defn make-exchange-table-model [session]
  (proxy [javax.swing.table.AbstractTableModel] []
    (getColumnCount [] (count columns))
    (getColumnName [i] (columns i))
    (getRowCount []
                 ;; store the reference used when row count
                 ;; returned so that we can use the same when
                 ;; call comes in to getValueAt
                 (dosync
                  (ref-set (:active-data-id-store session) @(:data-id-store session))
                  (count @(:active-data-id-store session))))
    (getValueAt [row col]
                ;; use the same reference that was used when row
                ;; count calculated                   
                (get-value-at session (@(:active-data-id-store session) row) col))))

(defn get-persistence-map [session]
  {:data-id-store @(:data-id-store session)
   :default-responses @(:default-responses session)})

(defn load-from-persistence-map! [session persistence-map]  
  (dosync   
   (ref-set (:data-id-store session) (:data-id-store persistence-map))
   (ref-set (:default-responses session) (:default-responses persistence-map)))
  (notify-data-changed session))

(defn get-exchange-for-row [session row]
  (exchange/get-exchange session (@(:data-id-store session) row)))

(defn get-value-at [session exchange-id col]
  ;; see the columns vector above
  (let [exchange (exchange/get-exchange session exchange-id)]    
    (cond
     (= col 0) (exchange/get-status exchange)     
     (= col 1) (exchange/get-namespace exchange)
     (= col 2) (exchange/get-uri exchange)
     (= col 3) (exchange/get-soap-method exchange)
     (= col 4) (exchange/get-start-date exchange)
     (= col 5) (exchange/get-end-date exchange)
     (= col 6) (exchange/get-request-time exchange)
     (= col 7) (if (= (exchange/get-id exchange)
                      (@(:default-responses session)
                        (make-default-response-key exchange)))
                 "R"
                 "")
     (= col 8) (exchange/get-num-replays exchange)
     (= col 9) (exchange/get-label exchange))))

(defn get-data-for-row
  "Returns the full xml body for the row"
  [session row key]
  (exchange/get-pretty-printed-body session (get-exchange-for-row session row) key))

(defn- add-entry!
  [session exchange]
  ;; only add the id once (on request)
  (dosync 
   (if (not (exchange/known? session exchange))
     (commute (:data-id-store session) conj (exchange/get-id exchange)))
   ;; but always update the data store
   (exchange/save! session exchange)))

(defn- notify-data-changed [session]
  (swing/do-swing
   (.fireTableDataChanged @(:exchange-table-model session))))

(defn- do-notification
  "Simple function to ensure the notification is always called start < end"
  [notification-fn a b]
  (let [start (if (< a b) a b)
        end (if (> b a) b a)]
    (log/info (format "Doing notification on range: %d %d" start end))
    (swing/do-swing
     (notification-fn start end))))

(defn- notify-row-changed
  ([session row]
     (notify-row-changed row row))
  ([session start end]
     (log/info (format "notify-row-changed %d %d" start end))
     (do-notification #(.fireTableRowsUpdated @(:exchange-table-model session)
                                              %1
                                              %2)
                      start end)))

(defn- notify-row-added
  ([session row]
     (notify-row-added session row row))
  ([session start end]
     (do-notification #(.fireTableRowsInserted @(:exchange-table-model session)
                                               %1 %2)
                      start end)))

(defn- notify-row-deleted
  ([session row]
     (log/info (format "Notify row deleted: %d" row))
     (notify-row-deleted session row row))
  ([session start end]
     (log/info (format "Notify rows deleted: %d to %d" start end))
     (do-notification #(.fireTableRowsDeleted @(:exchange-table-model session)
                                              %1 %2) start end)))

(defn add-exchange!
  "Add the exchange represented by map m"
  [session server m key]
  (let [exchange (exchange/init session m key)
        known? (exchange/known? session exchange)
        data-id-store (:data-id-store session)]    
    (add-entry! session exchange)
    (if known?
      (notify-row-changed session 0 (dec (count @data-id-store)))
      (notify-row-added session (dec (count @data-id-store))))
    exchange))

(defn- set-label-on-row [session label row]
  (log/info (format "Setting label on row %d to %s" row label))
  (let [exchange (get-exchange-for-row session row)]
    (exchange/set-label! session exchange label)
    0))

(defn set-label-on-rows! [session label rows]
  (amap rows idx ret (set-label-on-row session label (aget rows idx)))
  (notify-row-changed session (aget rows 0) (aget rows (dec (alength rows)))))

(defn update-exchange-body!
  "Updates the body on the currently selected exchange"
  [session text key row]
  (log/info (format "Updating %s body for row %d" key row))
  (let [exchange (get-exchange-for-row session row)]
    (exchange/update-body! session exchange key text)
    (notify-row-changed session row)))

(defn- get-default-response
  "Return the default response for this exchange type (if any)"
  [session exchange]
  (@(:default-responses session) (make-default-response-key exchange)))

(defn- delete-default-response-if-required!
  "Delete the default response if this is the response exchange"
  [session exchange]
  (let [response-exchange (get-default-response session exchange)]
    (if (= (exchange/get-id exchange/get-id)
           (exchange/get-id response-exchange))
      (log/info (format "Deleting default response id = %s"
                        (exchange/get-id exchange)))
      (commute (:default-responses session)
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
  [session row]
  (let [exchange (get-exchange-for-row session row)
        duplicate (exchange/duplicate session exchange)
        data-id-store (:data-id-store session)]
    (log/info (format "Duplicating row: %d" row))
    (dosync
     (ref-set data-id-store
              (vec (concat (subvec @data-id-store 0 row)
                           [(exchange/get-id duplicate)]
                           (subvec @data-id-store row))))
     (notify-row-added session (inc row))))
  0)

(defn duplicate-rows! [session rows]
  (row-loop rows (partial duplicate-row! session) nil))

(defn- delete-row! [session row]
  (let [exchange (get-exchange-for-row session row)
        data-id-store (:data-id-store session)]
    (log/info (format "Deleting row %d" row))
    (log/info (format "Num rows before delete = %d" (count @data-id-store)))
    (log/info (format "Num exchanges before delete = %d"
                      (exchange/get-num-exchanges session)))    
    (dosync
     (exchange/delete! session exchange)
     (ref-set data-id-store
              (vec (concat (subvec @data-id-store 0 row)
                           (subvec @data-id-store (inc row)))))
     (delete-default-response-if-required! session exchange))
    (log/info (format "Num rows after delete = %d" (count @data-id-store)))))

(defn delete-rows! [session rows]
  (log/info (format "Delete rows: %s" rows))
  (row-loop rows (partial delete-row! session) (partial notify-row-deleted session)))

(defn get-label-for-row [session row]
  (let [exchange (get-exchange-for-row session row)]
    (if (:label exchange) (:label exchange) "")))

(defn delete-label-on-rows! [session rows]  
  (set-label-on-rows! session nil rows))

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

(defn- set-default-response! [session row]  
  (let [exchange (get-exchange-for-row session row)
        key (make-default-response-key exchange)]
    ;; if already set then unset (toggle)
    (if (= (@(:default-responses session) key) (exchange/get-id exchange))
      (dosync
       (commute (:default-responses session) dissoc key)
       (log/info (format "Unset default response for row %d" row)))
      (dosync
       (commute (:default-responses session) assoc key (exchange/get-id exchange))
       (log/info (format "Set row %d as default response" row))))
    0))

(defn set-as-default-response! [session rows]
  (amap rows idx ret (set-default-response! session (aget rows idx)))
  (notify-row-changed session 0 (dec (count @(:data-id-store session)))))

(defn replay-response [session exchange]
  (let [response-id (get-default-response session exchange)]
    (if response-id
      (let [exchange (exchange/get-exchange session response-id)]
        (exchange/inc-replays! exchange)
        (notify-row-changed session 0 (dec (count @(:data-id-store session))))
        (:response exchange)))))
