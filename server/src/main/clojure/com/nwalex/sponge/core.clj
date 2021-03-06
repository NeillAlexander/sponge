; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.core
  (:use
   [clojure.main :only [repl]])
  (:require   
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.filters :as filters]
   [com.nwalex.sponge.datastore :as ds]
   [clojure.contrib.logging :as log]
   [clojure.contrib.command-line :as cmd-line]
   )
  (:gen-class :main true :name com.nwalex.sponge.Server)) 

(defn start-repl
  "Start the server wrapped in a repl. Use this to embed swank in your code."
  ([port]
     ;; had to do this using eval because of bug when pre-compiling
     ;; (or something). This way means we still have the functionality
     ;; but don't have slow compile time or strange hanging on --help
     (eval (let [stop (atom false)]
             (require 'swank.swank)
             (repl :read (fn [rprompt rexit]
                           (if @stop rexit
                               (do (swap! stop (fn [_] true))
                                   `(do (swank.swank/ignore-protocol-version nil)
                                        (swank.swank/start-server "slime-port.txt"
                                                            :encoding "iso-latin-1-unix"
                                                            :port ~port)))))
                   :need-prompt #(identity false)))))
  ([] (start-repl 4005)))

(defn -main [& args]  
  (cmd-line/with-command-line args
    "sponge --port [listening_port] --target [target server url]"
    [[port "Specify the port to listen on"]
     [target "The full address to forward requests onto"]
     [log? "Specify this to log the request / responses"]
     [swank? "Specify this to start a swank server"]
     [swank-port "Specify the port to start swank on (defaults to 4006)"]]
    (if (or (nil? port) (nil? target))
      (-main "--help")
      (try
       (let [request-filters (if log? [filters/logging-filter] [])
             response-filters (if log? [filters/logging-filter] [])
             server (server/start (server/make-server
                        (Integer/parseInt port)
                        target
                        :request-filters request-filters
                        :response-filters response-filters))
             swank-port-num (if swank-port (Integer/parseInt swank-port) 4006)]         
         (if swank?
           (start-repl swank-port-num))         
         server)
       (catch NumberFormatException ex
         (println (format "Invalid port number: %s" port)))))))
