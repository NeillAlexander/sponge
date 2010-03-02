(ns com.nwalex.sponge.core
  (:require   
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.filters :as filters]
   [clojure.contrib.logging :as log]
   [clojure.contrib.command-line :as cmd-line])
  (:gen-class :main true :name com.nwalex.sponge.Sponge)) 

(defn -main [& args]
  (cmd-line/with-command-line args
    "sponge --port [listening_port] --target [target server url]"
    [[port "Specify the port to listen on"]
     [target "The full address to forward requests onto"]
     [log? "Specify this to log the request / responses"]]
    (if (or (nil? port) (nil? target))
      (-main "--help")
      (try
       (let [request-filters (if log? [filters/logging-filter] [])
             response-filters (if log? [filters/logging-filter] [])]
         (server/start (server/make-server
                        (Integer/parseInt port)
                        target
                        :request-filters request-filters
                        :response-filters response-filters)))
       (catch NumberFormatException ex
         (println (format "Invalid port number: %s" port)))))))
