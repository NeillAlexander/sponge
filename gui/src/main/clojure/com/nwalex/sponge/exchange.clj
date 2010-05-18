; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

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

(defn- assign-id-to [session exchange]
  (if (:id exchange)
    exchange
    (do
      (assoc exchange :id (.getAndIncrement @(:next-exchange-id session))))))

(defn- new-data [exchange]
  "Create the data structure that represents the exchange"
  {:id (:id exchange)
   :num-replays 0
   :namespace (:namespace (soap/parse-soap (:request exchange)))
   :soap-method (:soap-method (soap/parse-soap (:request exchange)))})

(defn init
  "Initialize the exchange from the raw map m"
  [session m key]  
  (let [exchange-store (:exchange-store session)
        exchange (assign-id-to session m)
        data (if (@exchange-store (:id exchange))
               (@exchange-store (:id exchange))               
               (new-data exchange))
        time-key (if (= :request key) :started :ended)]
    (assoc data
      key (key exchange)
      time-key (System/currentTimeMillis))))

(defn get-id [exchange] (:id exchange))

(defn save!
  "Update the exchange to be this new value"
  [session exchange]
  ;; there is a bug that led to corrupt data
  ;; in order to track this down ensure there
  ;; is a request for the exchange and throw
  ;; an exception if there isn't
  (if-not (:request exchange)
    (do
      (log/error (format "Corrupt exchange data:\n%s" exchange))
      (throw (RuntimeException. "Uh oh. Corrupt data detected. Abort, abort!"))))
  (dosync
   (commute (:exchange-store session) assoc (:id exchange) exchange))
  exchange)

(defn delete!
  [session exchange]
  (log/info (format "Deleting exchange id: %s" (get-id exchange)))
  (dosync
   (commute (:exchange-store session) dissoc (:id exchange))
   (commute (:replay-count session) dissoc (:id exchange))))

(defn get-exchange
  "Return the exchange with the specified id"
  [session id]
  (@(:exchange-store session) id))

(defn duplicate [session exchange]
  (let [dup (assign-id-to session (dissoc exchange :id))]
    (save! session (assoc dup :num-replays 0))
    (get-exchange session (:id dup))))

(defn get-body
  "Key is either :request or :response"
  [exchange key]
  (:body (key exchange)))

(defn update-body!
  "Update the body of the exchange"
  [session exchange key text]
  (let [exchange-store (:exchange-store session)]
    (dosync
     (commute exchange-store assoc-in [(:id exchange) key :body] text)
     (commute exchange-store assoc-in [(:id exchange) key :pretty-printed] false))))

(defn get-num-replays [session exchange]
  (let [count ((:replay-count session) (:id exchange))]
    (if count count 0)))

(defn inc-replays!
  "Increment the replay account on the exchange"
  [session exchange]
  (dosync
   (commute (:replay-count session)
            assoc (:id exchange) (inc (get-num-replays exchange)))))

(defn set-label!
  [session exchange label]
  (dosync
   (commute (:exchange-store session) assoc (:id exchange)
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

(defn pretty-print [session exchange key]
  (log/info (format "Pretty printing %s id %d" key (:id exchange)))
  (let [pp-body (do-pretty-print (:body (key exchange)))
        pp-data-key (assoc (key exchange)
                      :body pp-body
                      :pretty-printed true)
        pp-data (assoc exchange key pp-data-key)]
    (save! session pp-data)))

(defn- has-body? [exchange key]
  (:body (key exchange)))

(defn get-pretty-printed-body [session exchange key]  
  (if (:pretty-printed (key exchange))
    (:body (key exchange))
    (if (has-body? exchange key)
      (:body (key (pretty-print session exchange key)))
      "")))

(defn get-status
  [exchange]
  (if (:response exchange) (:status (:response exchange)) "Requesting..."))

(defn get-namespace
  [exchange]
  (:namespace exchange))

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
  [session exchange]
  (@(:exchange-store session) (:id exchange)))

(defn get-num-exchanges [session]
  (count (keys @(:exchange-store session))))

(defn get-persistence-map [session]
  {:exchange-store @(:exchange-store session)
   :next-exchange-id (.get @(:next-exchange-id session))})

(defn load-from-persistence-map! [session persistence-map]  
  (dosync   
   (ref-set (:exchange-store session) (:exchange-store persistence-map))
   (ref-set (:next-exchange-id session) (java.util.concurrent.atomic.AtomicLong.
                                 (:next-exchange-id persistence-map)))))

