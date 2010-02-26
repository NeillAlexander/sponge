(ns com.nwalex.sponge.core
  (:require
    [ring.adapter.jetty :as jetty]
    [ring.middleware.reload :as reload]
    [com.nwalex.sponge.http :as http]
    [clojure.contrib.logging :as log]
    [clojure.contrib.command-line :as cmd-line])
  (:gen-class :main true :name com.nwalex.sponge.Sponge))

(defn handle-request [target req]
  (let [response (http/forward-request target req)]    
    (log/info (format "response = %s" response))
    {:status  200
     :headers {"Content-Type" "text/xml;charset=utf-8"}
     :body    response}))

(defn app [target req]
  (handle-request target req))

(defn with-reload-app [target]
  (reload/wrap-reload #(app target %1) '(com.nwalex.sponge.core))) 

(defn start
  "Start and return instance of server"
  [port target]
  (jetty/run-jetty (with-reload-app target) {:port port :join false}))

(defn stop [server]
  (.stop server))

(defn -main [& args]
  (cmd-line/with-command-line args
    "sponge --port [listening_port] --target [target server url]"
    [[port "Specify the port to listen on"]
     [target "The full address to forward requests onto"]]
    (try
     (start (Integer/parseInt port) target)
     (catch NumberFormatException ex
       (println (format "Invalid port number: %s" port))))))
