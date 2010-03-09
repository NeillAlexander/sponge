(ns com.nwalex.sponge.table-model
  (:require
   [clojure.contrib.swing-utils :as swing]
   [clojure.contrib.zip-filter :as zipf]
   [clojure.contrib.zip-filter.xml :as zipfx]
   [clojure.contrib.logging :as log]
   [clojure.zip :as zip]
   [clojure.xml :as xml]))

(declare get-value-at)

(def #^{:private true} data-id-store (ref []))
(def #^{:private true} table-data-store (ref {}))
(def #^{:private true} id-store
     (atom (java.util.concurrent.atomic.AtomicLong.)))

(def #^{:private true} columns ["Status" "Namespace" "URL" "Soap Method"
                                "Start" "End" "Time (ms)" "Label"])

(def #^{:private true} exchange-table-model
     (proxy [javax.swing.table.AbstractTableModel] []
       (getColumnCount [] (count columns))
       (getColumnName [i] (columns i))
       (getRowCount [] (count @data-id-store))
       (getValueAt [row col] (get-value-at row col))))

(defn- calc-status-from [data]
  (if (:response data) (:status (:response data)) "Requesting..."))

(defn- format-date [time-ms]
  (if time-ms
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss.SSS")
             (java.util.Date. time-ms))
    "n/a"))

;; this is a bit hacky and may break!! Relying on the fact that
;; always have xmlns in the body and that the soap method is the
;; name of the first child of the body. Otherwise will need smarter
;; xml parsing here
(defn- parse-soap [request]
  (let [zipped (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream.
                                        (.getBytes (:body request)))))]
    {:namespace (:xmlns (:attrs
                           (first (:content (first (:content (first zipped)))))))
     :soap-method (.substring (str
                            (:tag (first (:content
                                          (first (:content (first zipped))))))) 1)}))

(defn- get-table-data-for-row [row]
  (@table-data-store (@data-id-store row)))

(defn get-value-at [row col]
  ;; see the columns vector above
  (let [data (get-table-data-for-row row)]    
    (cond
     (= col 0) (calc-status-from data)     
     (= col 1) (:namespace (parse-soap (:request data)))
     (= col 2) (:uri (:request data))
     (= col 3) (:soap-method (parse-soap (:request data)))
     (= col 4) (format-date (:started data))
     (= col 5) (format-date (:ended data))
     (= col 6) (if (:ended data)
                 (- (:ended data) (:started data))
                 "n/a")
     (= col 7) (if (:label data) (:label data) ""))))

(defn- do-pretty-print [body]
  (let [format (org.dom4j.io.OutputFormat/createPrettyPrint)
        document (org.dom4j.DocumentHelper/parseText body)
        sw (java.io.StringWriter.)
        xml-writer (org.dom4j.io.XMLWriter. sw format)]
    (try
     (.write xml-writer document)
     (.toString sw)
     (catch Exception ex
       body))))

(defn- pretty-print [data key]
  (log/info (format "Pretty printing %s id %d" key (:id data)))
  (let [pp-body (do-pretty-print (:body (key data)))
        pp-data-key (assoc (key data)
                      :body pp-body
                      :pretty-printed true)
        pp-data (assoc data key pp-data-key)]    
    (dosync
     (commute table-data-store assoc (:id data) pp-data))
    (@table-data-store (:id data))))

(defn- pretty-printed-body [data key]  
  (if (:pretty-printed (key data))
    (:body (key data))
    (:body (key (pretty-print data key)))))

(defn get-data-for-row
  "Returns the full xml body for the row"
  [row key]
  (pretty-printed-body (get-table-data-for-row row) key))

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
  (dosync 
   (if (not-already-saved table-data)
     (commute data-id-store conj (:id table-data)))
   ;; but always update the data store
   (commute table-data-store assoc (:id table-data) table-data)))

(defn- notify-data-changed []
  (swing/do-swing
     (.fireTableDataChanged exchange-table-model)))

(defn add-exchange! [server exchange key]
  (let [exchange-with-id (assign-id-to exchange)]
    (add-entry (make-table-data exchange-with-id key))
    (notify-data-changed)
    exchange-with-id))

(defn get-table-model []
  exchange-table-model)

(defn clear [event]
  (dosync
   (ref-set data-id-store [])
   (ref-set table-data-store {}))
  (notify-data-changed))

(defn set-label-on-row [label row]
  (let [table-data (get-table-data-for-row row)]
    (dosync
     (commute table-data-store assoc (:id table-data)
              (assoc table-data :label label)))
    (notify-data-changed)))

(defn get-label-for-row [row]
  (let [data (get-table-data-for-row row)]
    (if (:label data) (:label data) "")))

(defn delete-label-on-row [row]
  (set-label-on-row nil row))
