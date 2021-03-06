; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.filters
  (:require
   [com.nwalex.sponge.datastore :as ds]
   [com.nwalex.sponge.http :as http]
   [clojure.contrib.logging :as log]))

(defn continue
  "Filter should call this to carry on processing"
  [exchange]
  {:continue exchange})

(defn return
  [exchange]
  {:return exchange})

(defn abort
  [exchange]
  {:abort exchange})

(defn continue? [response] (:continue response))
(defn return? [response] (:return response))
(defn abort? [response] (:abort response))

(defn logging-filter
  "Simply logs exchange to stdout"
  [server exchange key]
  (log/info (key exchange))
  (continue exchange))

(defn datastore-filter
  "Stores the exchanges for future reference"
  [server exchange key]
  (ds/add-exchange server exchange)
  (continue exchange))

(defn forwarding-request-filter
  "This is the default filter, the last one in the list of request filters"
  [server exchange key]
  (log/info "In forwarding-request-filter")
  (let [req (:request exchange)
        response (http/forward-request (:target server) req)]    
    (log/info (format "Read response from: %s" (:target server)))    
    (return (assoc exchange :response response))))

(defn returning-response-filter
  "Identity filter - just returns the exchange"
  [server exchange key]
  (return exchange))
