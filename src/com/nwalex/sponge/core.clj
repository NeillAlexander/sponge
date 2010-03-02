(ns com.nwalex.sponge.core
  (:require   
   [com.nwalex.sponge.server :as server]      
   [clojure.contrib.logging :as log]
   [clojure.contrib.command-line :as cmd-line])
  (:gen-class :main true :name com.nwalex.sponge.Sponge)) 

(defn -main [& args]
  (cmd-line/with-command-line args
    "sponge --port [listening_port] --target [target server url]"
    [[port "Specify the port to listen on"]
     [target "The full address to forward requests onto"]]
    (if (or (nil? port) (nil? target))
      (-main "--help")
      (try
       (server/start (server/make-server (Integer/parseInt port) target))
       (catch NumberFormatException ex
         (println (format "Invalid port number: %s" port)))))))
