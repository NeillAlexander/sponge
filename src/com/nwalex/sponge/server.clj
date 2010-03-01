(ns com.nwalex.sponge.server
  (:require
   [com.nwalex.sponge.http :as http]
   [clojure.contrib.logging :as log]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :as reload]))

(defn- handle-request [target req]
  (let [response (http/forward-request target req)]    
    (log/info (format "Read response from: %s" target))
    {:status  200
     :headers {"Content-Type" "text/xml;charset=utf-8"}
     :body    response}))

(defn- app [target req]
  (handle-request target req))

(defn- with-reload-app [target]
  (reload/wrap-reload #(app target %1) '(com.nwalex.sponge.core)))

(defn make-server
  "Create an instance of the Sponge server. Options are as follows:
  :request-handlers  [f1 f2 ... fx]
  :response-handlers [f1 f2 ... fx]"
  [port target & opts]
  (let [opts-map (apply array-map opts)]
    {:port port :target target :jetty nil
     :request-handlers (:request-handlers opts-map)
     :response-handlers (:response-handlers opts-map)}))

(defn start [server]
  (assoc server :jetty (jetty/run-jetty
                        (with-reload-app
                          (:target server))
                        {:port (:port server) :join false})))

(defn stop [server]
  (.stop (:jetty server)))

(defn running? [server]
  (.isRunning (:jetty server)))
