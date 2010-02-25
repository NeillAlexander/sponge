(ns com.nwalex.sponge.core-test
  (:use
   [clojure.test])
  (:require
   [ring.adapter.jetty :as jetty]
   [com.nwalex.sponge.http :as http]
   [com.nwalex.sponge.core :as sponge] :reload-all))


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
  (let [sponge (sponge/start)
        responder (start-app pong-app 8140)]
    (is (.isRunning sponge))
    (is (.isRunning responder))
    (is (= "pong\n\n" (http/send-request "hello" "http://localhost:8139")))
    (.stop sponge)
    (.stop responder)
    (is (not (.isRunning sponge)))
    (is (not (.isRunning responder)))))

