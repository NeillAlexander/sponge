(ns com.nwalex.sponge.server
  (:require
   [com.nwalex.sponge.http :as http]
   [clojure.contrib.logging :as log]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :as reload]))

(defn- handle-request [server req]
  (loop [rh (first (:request-handlers server))
         other-rh (rest (:request-handlers server))]
    ;; TODO: finish this logic off
    (let [response (rh server req)]
      (if (:return response)
        (:return response)
        (recur (first other-rh) (rest other-rh))))))

(defn- app [server req]  
  (handle-request server req))

(defn- with-reload-app [server]
  (reload/wrap-reload #(app server %1) '(com.nwalex.sponge.core)))

(defn- forwarding-request-handler
  "This is the default handler, the last one in the list of request handlers"
  [server req]
  (log/info "In forwarding-request-handler")
  (let [response (http/forward-request (:target server) req)]    
    (log/info (format "Read response from: %s" (:target server)))
    {:return
     {:status  200
      :headers {"Content-Type" "text/xml;charset=utf-8"}
      :body    response}}))

(defn make-server
  "Create an instance of the Sponge server. Options are as follows:
  :request-handlers  [f1 f2 ... fx]
  :response-handlers [f1 f2 ... fx]"
  [port target & opts]
  (let [opts-map (apply array-map opts)]
    {:port port :target target :jetty nil
     :request-handlers (conj (vec (:request-handlers opts-map)) forwarding-request-handler)
     :response-handlers (vec (:response-handlers opts-map))}))

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
