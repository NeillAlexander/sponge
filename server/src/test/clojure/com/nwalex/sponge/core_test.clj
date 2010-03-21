; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software


(ns com.nwalex.sponge.core-test
  (:use
   [clojure.test])
  (:require
   [ring.adapter.jetty :as jetty]
   [clojure.contrib.logging :as log]
   [com.nwalex.sponge.http :as http]
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.core :as core] :reload-all))


(defn echo-app [req]
  {:status 200
   :headers {"Content-Type" "text/xml;charset=utf-8"}
   :body (:body req)})

(defn pong-app [req]
  {:status 200
   :headers {"Content-Type" "text/plain;charset=utf-8" "Connection" "close"}
   :body "pong"}
  )

(defn start-app [app-fn port]
  (jetty/run-jetty app-fn {:port port :join false}))

(defmacro with-responder
  "Starts up a sponge server and responder, executes the body, then stops them again"
  [server & body]
  `(let [server# (if (not (server/running? ~server))
                   (server/start ~server)
                   ~server)
         responder# (start-app pong-app 8150)]
     (is (server/running? server#))
     (is (.isRunning responder#))
     (try
      (do ~@body)
      (finally
       (do
         (server/stop server#)
         (.stop responder#)
         (is (not (server/running? server#)))
         (is (not (.isRunning responder#))))))))

(deftest test-server
  (let [server (core/-main "--port" "8149"
                           "--target" "http://localhost:8150")]
    (with-responder server
      (let [response (http/send-request
                      "hello" "http://localhost:8149")]
        (log/info (format "test-server response = %s" response))
        (is (.startsWith (:body response) "pong"))))))

(deftest make-server-test-no-filters
  (let [server (server/make-server 8747 "addr")]
    (is (= 8747 (:port server)))
    (is (= "addr" (:target server)))
    (is (= 1 (count (:request-filters server))))
    (is (= 1 (count (:response-filters server))))))

(deftest make-server-test-with-filters
  (let [server (server/make-server 8747 "addr"
                                   :request-filters '[a b c]
                                   :response-filters '[d e f])]
    (is (= 8747 (:port server)))
    (is (= "addr" (:target server)))
    (is (= 4 (count (:request-filters server))))
    (is (= 4 (count (:response-filters server))))))

(defn- cont [flag-atom server exchange key]
  (swap! flag-atom (fn [old] true))
  {:continue exchange})

(deftest test-continue
  (let [called1 (atom false)
        called2 (atom false)
        called3 (atom false)
        cont1 (partial cont called1)
        cont2 (partial cont called2)
        cont3 (partial cont called3)
        server (server/make-server
                    8149
                    "http://localhost:8150"
                    :request-filters [cont1 cont2 cont3])]    
    (with-responder server
      (is (.startsWith (:body (http/send-request
                               "hello" "http://localhost:8149"))
                       "pong"))
      (is @called1)
      (is @called2)
      (is @called3))))

(defn- abort [flag-atom server exchange key]
  (swap! flag-atom (fn [old] true))
  {:abort exchange})

(deftest test-abort
  (let [called1 (atom false)
        called2 (atom false)
        abort1 (partial abort called1)
        abort2 (partial abort called2)
        server (server/make-server
                    8149
                    "http://localhost:8150"
                    :request-filters [abort1 abort2])]
    (with-responder server
      (http/send-request "hello" "http://localhost:8149")
      (is @called1)
      (is (not @called2)))))
