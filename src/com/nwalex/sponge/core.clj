(ns com.nwalex.sponge.core
  [:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :as reload]
   [clojure.contrib.duck-streams :as ds]]
  [:import
   [org.apache.commons.httpclient HttpClient]
   [org.apache.commons.httpclient.methods PostMethod StringRequestEntity]])

;; TODO: factor this out into a httpclient namespace
(defn forward-request
  "Forward the soap request on to the configured host / port"
  [host port req]
  (let [xml (ds/slurp* (:body req)) 
        client (HttpClient.)
        post (PostMethod. (format "%s:%d%s" host port (:uri req)))]
    ;; TODO: remove this
    (println req)
    (println (format "request = %s" xml))
    ;; set up the request
    (.setRequestEntity post
                       (StringRequestEntity. xml "text/xml" "utf-8"))
    (.executeMethod client post)
    ;; return the response
    ;; TODO: return a response map like Ring expects with the actual
    ;; status code etc
    (let [response (String. (.getResponseBody post))]
      ;; TODO: handle this in a try / catch / finally
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
  (jetty/run-jetty with-reload-app {:port 8139 :join false}))

(defn stop [server]
  (.stop server))
