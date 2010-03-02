(ns com.nwalex.sponge.server
  (:require
   [com.nwalex.sponge.http :as http]
   [clojure.contrib.logging :as log]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :as reload]))

(defn- process-filters
  "Factors out the functionality common to request / response filters"
  [filters str server req-res]
  (loop [rh (first filters)
         other-rh (rest filters)
         arg req-res]
    (log/info (format "Processing %s..." str))
    (let [result (rh server arg)]
      ;; check the responses to determine whether to continue etc
      (cond
       (:return result) (do
                          (log/info (format "Returning %s..." str))
                            (:return result))
       (:continue result) (do
                              (log/info "Continuing...")
                              (recur (first other-rh)
                                     (rest other-rh)
                                     (:continue result)))
       (:abort result) (do
                           (log/info "Aborting...")
                           (throw (RuntimeException. "Request aborted")))
       :else (throw (IllegalStateException.
                     ":return / :continue / :abort not found"))))))

(defn- process-request
  "Processes the request passing it through the configured filters"
  [server req]
  (process-filters (:request-filters server) "request" server req))

(defn- process-response
  [server response]
  (process-filters (:response-filters server) "response" server response))

(defn- handle-request  
  [server req]
  (process-response server (process-request server req)))

(defn- app [server req]  
  (handle-request server req))

(defn- with-reload-app [server]
  (reload/wrap-reload #(app server %1) '(com.nwalex.sponge.core)))

(defn- forwarding-request-filter
  "This is the default filter, the last one in the list of request filters"
  [server req]
  (log/info "In forwarding-request-filter")
  (let [response (http/forward-request (:target server) req)]    
    (log/info (format "Read response from: %s" (:target server)))
    {:return
     {:status  200
      :headers {"Content-Type" "text/xml;charset=utf-8"}
      :body    response}}))

(defn- returning-response-filter
  [server response]
  {:return response})

(defn make-server
  "Create an instance of the Sponge server. Options are as follows:
  :request-filters  [f1 f2 ... fx]
  :response-filters [f1 f2 ... fx]"
  [port target & opts]
  (let [opts-map (apply array-map opts)]
    {:port port :target target :jetty nil
     :request-filters (conj
                        (vec (:request-filters opts-map))
                        forwarding-request-filter)
     :response-filters (conj
                         (vec (:response-filters opts-map))
                         returning-response-filter)}))

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
