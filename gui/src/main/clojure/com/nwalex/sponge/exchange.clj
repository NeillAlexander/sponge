(ns com.nwalex.sponge.exchange
  (:require
   [com.nwalex.sponge.soap :as soap]
   [clojure.contrib.logging :as log]))

;; When the server gets a request it wraps it
;; up in an 'exchange' which is simply a map

(comment
  "This is what the raw exchange looks like that comes
from the server"
  {:request request-body :response nil})

(def #^{:private true} exchange-store (ref {}))
(def #^{:private true} next-exchange-id
     (ref (java.util.concurrent.atomic.AtomicLong.)))

(defn- assign-id-to [exchange]
  (if (:id exchange)
    exchange
    (do
      (log/info (format "Assigning id to %s " exchange))
      (assoc exchange :id (.getAndIncrement @next-exchange-id)))))

(defn- new-data [exchange]
  "Create the data structure that represents the exchange"
  {:id (:id exchange)
   :num-replays 0
   :namespace (:namespace (soap/parse-soap (:request exchange)))
   :soap-method (:soap-method (soap/parse-soap (:request exchange)))})

(defn init
  "Initialize the exchange from the raw map m"
  [m key]
  (let [exchange (assign-id-to m)
        data (if (@exchange-store (:id exchange))
               (@exchange-store (:id exchange))               
               (new-data exchange))
        time-key (if (= :request key) :started :ended)]
    (log/info (format "init ex: %s" exchange))
    (log/info (format "init data: %s" exchange))
    (assoc data
      key (key exchange)
      time-key (System/currentTimeMillis))))

(defn save
  "Update the exchange to be this new value"
  [exchange]
  (log/info (format "Saving: %s" exchange))
  (dosync
   (commute exchange-store assoc (:id exchange) exchange))
  exchange)

(defn delete-all
  "Delete all the stored exchanges"
  []
  (dosync
   (ref-set exchange-store {})))

(defn delete
  [exchange]
  (dosync
   (commute exchange-store dissoc (:id exchange))))

(defn get-exchange
  "Return the exchange with the specified id"
  [id]
  (@exchange-store id))

(defn get-body
  "Key is either :request or :response"
  [exchange key]
  (:body (key exchange)))

(defn inc-replays
  "Increment the replay account on the exchange"
  [exchange]
  (dosync
   (commute exchange-store assoc (:id exchange)
            (assoc exchange :num-replays (inc
                                          (:num-replays exchange))))))

(defn set-label
  [exchange label]
  (dosync
   (commute exchange-store assoc (:id exchange)
            (assoc exchange :label label))))

(defn get-label [exchange]
  (if (:label exchange) (:label exchange) ""))

(defn do-pretty-print
  "Expects body to be valid xml"
  [body]
  (try
   (let [format (org.dom4j.io.OutputFormat/createPrettyPrint)
         document (org.dom4j.DocumentHelper/parseText body)
         sw (java.io.StringWriter.)
         xml-writer (org.dom4j.io.XMLWriter. sw format)]
     (.write xml-writer document)
     (.toString sw))
   (catch Exception ex
       body)))

(defn pretty-print [exchange key]
  (log/info (format "Pretty printing %s id %d" key (:id exchange)))
  (let [pp-body (do-pretty-print (:body (key exchange)))
        pp-data-key (assoc (key exchange)
                      :body pp-body
                      :pretty-printed true)
        pp-data (assoc exchange key pp-data-key)]
    (save pp-data)))

(defn get-pretty-printed-body [exchange key]  
  (if (:pretty-printed (key exchange))
    (:body (key exchange))
    (:body (key (pretty-print exchange key)))))

(defn get-status
  [exchange]
  (if (:response exchange) (:status (:response exchange)) "Requesting..."))

(defn get-namespace
  [exchange]
  (:namespace exchange))

(defn get-id [exchange] (:id exchange))

(defn get-num-replays [exchange] (:num-replays exchange))

(defn get-uri
  [exchange]
  (:uri (:request exchange)))

(defn get-soap-method
  [exchange]
  (:soap-method exchange))

(defn- format-date [time-ms]
  (if time-ms
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss.SSS")
             (java.util.Date. time-ms))
    "n/a"))

(defn get-start-date
  [exchange]
  (format-date (:started exchange)))

(defn get-end-date
  [exchange]
  (format-date (:ended exchange)))

(defn get-request-time
  [exchange]
  (if (:ended exchange)
    (- (:ended exchange) (:started exchange))
    "n/a"))

(defn known?
  "Returns true if exchange of this id exists"
  [exchange]
  (@exchange-store (:id exchange)))

(defn get-persistence-map []
  {:exchange-store @exchange-store
   :next-exchange-id (.get @next-exchange-id)})

(defn load-from-persistence-map [persistence-map]  
  (dosync   
   (ref-set exchange-store (:exchange-store persistence-map))
   (ref-set next-exchange-id (java.util.concurrent.atomic.AtomicLong.
                              (:next-exchange-id persistence-map)))))

