(ns com.nwalex.sponge.core-test
  (:use
   [clojure.test])
  (:require
   [ring.adapter.jetty :as jetty]
   [com.nwalex.sponge.http :as http]
   [com.nwalex.sponge.server :as server]
   [com.nwalex.sponge.core :as core] :reload-all))


(defn echo-app [req]
  {:status 200
   :headers {"Content-Type" "text/xml;charset=utf-8"}
   :body (:body req)})

(defn pong-app [req]
  {:status 200
   :headers {"Content-Type" "text/plain;charset=utf-8"}
   :body "pong"}
  )

(defn start-app [app-fn port]
  (jetty/run-jetty app-fn {:port port :join false}))


(deftest test-server
  (let [server (core/-main "--port" "8149" "--target" "http://localhost:8150")
        responder (start-app pong-app 8150)]
    (is (server/running? server))
    (is (.isRunning responder))
    (is (.startsWith (http/send-request "hello" "http://localhost:8149") "pong"))
    (server/stop server)
    (.stop responder)
    (is (not (server/running? server)))
    (is (not (.isRunning responder)))))

