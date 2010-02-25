(ns com.nwalex.sponge.core-test
  (:use [com.nwalex.sponge.core] :reload-all)
  (:use [clojure.test])
  (:require [ring.adapter.jetty :as jetty])
  (:import [org.apache.commons.httpclient HttpClient]
           [org.apache.commons.httpclient.methods PostMethod StringRequestEntity]))


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


(defn send-request [text port]
  (let [client (HttpClient.)
        post (PostMethod. (format "http://localhost:%d" port))]
    (.setRequestEntity post
                       (StringRequestEntity. text "text/plain" "utf-8"))
    (.executeMethod client post)
    (let [response (String. (.getResponseBody post))]
      ;; TODO: handle this in a try / catch / finally
      (.releaseConnection post)
      response)))

(deftest test-server
  (let [sponge (start)
        responder (start-app pong-app 8140)]
    (is (.isRunning sponge))
    (is (.isRunning responder))
    (is (= "pong\n\n" (send-request "hello" 8139)))
    (.stop sponge)
    (.stop responder)
    (is (not (.isRunning sponge)))
    (is (not (.isRunning responder)))))
