(ns com.nwalex.sponge.core
  (:require
    [ring.adapter.jetty :as jetty]
    [ring.middleware.reload :as reload]
    [com.nwalex.sponge.http :as http]
    [clojure.contrib.logging :as log]))

(defn handle-request [req]
  (let [response (http/forward-request "http://localhost" 8140 req)]    
    (log/info (format "response = %s" response))
    {:status  200
     :headers {"Content-Type" "text/xml;charset=utf-8"}
     :body    response}))

(defn app [req]
  (handle-request req))

(def with-reload-app (reload/wrap-reload app '(com.nwalex.sponge.core))) 

(defn start
  "Start and return instance of server"
  []
  (jetty/run-jetty with-reload-app {:port 8139 :join false}))

(defn stop [server]
  (.stop server))
