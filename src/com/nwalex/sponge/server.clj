(ns com.nwalex.sponge.server
  (:require
   [com.nwalex.sponge.http :as http]
   [com.nwalex.sponge.datastore :as ds]
   [com.nwalex.sponge.filters :as filters]
   [clojure.contrib.logging :as log]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :as reload]))

(defn- process-filters
  "Factors out the functionality common to request / response filters"
  [filters server exchange exchange-key]
  (loop [exchange-filter (first filters)
         remaining-filters (rest filters)
         filter-arg exchange]
    (log/info (format "Processing %s..." exchange-key))
    (let [result (exchange-filter server filter-arg exchange-key)]
      ;; check the responses to determine whether to continue etc
      (cond
       (:return result) (do
                          (log/info (format "Returning %s..." exchange-key))
                          (:return result))
       (:continue result) (do
                              (log/info "Continuing...")
                              (recur (first remaining-filters)
                                     (rest remaining-filters)
                                     (:continue result)))
       (:abort result) (do
                           (log/info "Aborting...")
                           (throw (RuntimeException. "Request aborted")))
       :else (throw (IllegalStateException.
                     ":return / :continue / :abort not found"))))))

(defn- process-request
  "Processes the request passing it through the configured filters"
  [server exchange]
  (process-filters (:request-filters server) server exchange :request))

(defn- process-response
  [server exchange]
  (process-filters (:response-filters server) server exchange :response))

(defn- handle-request
  "This is the entry point for the Ring requests"
  [server req]
  (let [exchange {:request req :response nil}
        response (process-response server (process-request server exchange))]
    (:response response)))

(defn- app [server req]  
  (handle-request server req))

(defn- with-reload-app [server]
  (reload/wrap-reload #(app server %1) '(com.nwalex.sponge.core)))

(defn make-server
  "Create an instance of the Sponge server. Options are as follows:
  :request-filters  [f1 f2 ... fx]
  :response-filters [f1 f2 ... fx]"
  [port target & opts]
  (let [opts-map (apply array-map opts)
        server {:port port :target target :jetty nil
     :request-filters (conj
                        (vec (:request-filters opts-map))
                        filters/forwarding-request-filter)
     :response-filters (conj
                        (vec (:response-filters opts-map))
                        filters/datastore-filter
                        filters/returning-response-filter)}]
    (ds/make-datastore server)))

(defn start [server]
  (assoc server :jetty (jetty/run-jetty
                        (with-reload-app server)
                        {:port (:port server) :join false})))

(defn stop [server]
  (if (:jetty server) (.stop (:jetty server))))

(defn running? [server]
  (if (:jetty server)
    (.isRunning (:jetty server))
    false))
