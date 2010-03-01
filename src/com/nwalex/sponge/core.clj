(ns com.nwalex.sponge.core
  (:require
    [ring.adapter.jetty :as jetty]
    [ring.middleware.reload :as reload]
    [com.nwalex.sponge.http :as http]
    [clojure.contrib.logging :as log]
    [clojure.contrib.command-line :as cmd-line])
  (:gen-class :main true :name com.nwalex.sponge.Sponge))

(defn- configure-log4j []
  (let [props (java.util.Properties.)]
    (doto props
      (.setProperty "log4j.rootLogger" "INFO, A1")
      (.setProperty "log4j.appender.A1" "org.apache.log4j.ConsoleAppender")
      (.setProperty "log4j.appender.A1.layout" "org.apache.log4j.PatternLayout")
      (.setProperty "log4j.appender.A1.layout.ConversionPattern" "%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5p %c %x - %m%n"))
    (org.apache.log4j.PropertyConfigurator/configure props)))

(defn handle-request [target req]
  (let [response (http/forward-request target req)]    
    (log/info (format "Read response from: %s" target))
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
  (configure-log4j)
  (cmd-line/with-command-line args
    "sponge --port [listening_port] --target [target server url]"
    [[port "Specify the port to listen on"]
     [target "The full address to forward requests onto"]]
    (if (or (nil? port) (nil? target))
      (-main "--help")
      (try
       (start (Integer/parseInt port) target)
       (catch NumberFormatException ex
         (println (format "Invalid port number: %s" port)))))))
