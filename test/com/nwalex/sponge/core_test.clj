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
      (is (.startsWith (http/send-request
                        "hello" "http://localhost:8149") "pong")))))

(deftest make-server-test-no-handlers
  (let [server (server/make-server 8747 "addr")]
    (is (= 8747 (:port server)))
    (is (= "addr" (:target server)))
    (is (= 1 (count (:request-handlers server))))
    (is (= 0 (count (:response-handlers server))))))

(deftest make-server-test-with-handlers
  (let [server (server/make-server 8747 "addr"
                                   :request-handlers '[a b c]
                                   :response-handlers '[d e f])]
    (is (= 8747 (:port server)))
    (is (= "addr" (:target server)))
    (is (= 4 (count (:request-handlers server))))
    (is (= 3 (count (:response-handlers server))))))

(deftest test-continue
  (let [called (atom false)]    
    (letfn [(cont1
             [server req]
             (swap! called (fn [old] true))
             {:continue req})]
      (let [server (server/make-server
                    8149
                    "http://localhost:8150"
                    :request-handlers [cont1])]
        (with-responder server
          (is (.startsWith (http/send-request
                            "hello" "http://localhost:8149") "pong"))
          (is @called))))))
