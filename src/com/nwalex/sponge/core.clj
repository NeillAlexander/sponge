(ns com.nwalex.sponge.core
  [:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :as reload]
   [clojure.contrib.duck-streams :as ds]]
  [:import
   [org.apache.commons.httpclient HttpClient]
   [org.apache.commons.httpclient.methods PostMethod StringRequestEntity]])

(defn forward-request
  "Forward the soap request on to the configured host / port"
  [host port req]
  (let [xml (ds/slurp* (:body req)) 
        client (HttpClient.)
        post (PostMethod. (format "%s:%d%s" host port (:uri req)))]
    (println req)
    (println (format "request = %s" xml))
    (.setRequestEntity post
                       (StringRequestEntity. xml "text/xml" "utf-8"))
    (.executeMethod client post)
    (let [response (String. (.getResponseBody post))]
      (.releaseConnection post)
      response)))

(defn handle-request [req]
  (let [response (forward-request "http://localhost" 8140 req)]    
    (println (format "response = %s" response))
    {:status  200
     :headers {"Content-Type" "text/xml;charset=utf-8"}
     :body    response}))

(defn app [req]
  (handle-request req))

(def with-reload-app (reload/wrap-reload app '(com.nwalex.sponge.core))) 

(defn start
  "Start and return instance of server"
  []
  (jetty/run-jetty with-reload-app {:port 8080 :join false}))

(defn stop [server]
  (.stop server))
