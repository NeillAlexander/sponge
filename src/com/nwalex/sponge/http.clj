(ns com.nwalex.sponge.http
  (:import
   [org.apache.commons.httpclient HttpClient]
   [org.apache.commons.httpclient.methods PostMethod StringRequestEntity])
  (:require
   [clojure.contrib.duck-streams :as ds]))

(defn send-request
  "Creates a PostMethod setting the body to text, and sends to address"
  [text address]
  (let [client (HttpClient.)
        post (PostMethod. address)]
    (.setRequestEntity post
                       (StringRequestEntity. text "text/xml" "utf-8"))
    (.executeMethod client post)
    (let [response (String. (.getResponseBody post))]
      ;; TODO: handle this in a try / catch / finally
      (.releaseConnection post)
      response)))

(defn forward-request
  "Forward the soap request on to the configured host / port"
  [host port req]
  (let [xml (ds/slurp* (:body req)) 
        address (format "%s:%d%s" host port (:uri req))]
    ;; TODO: remove this
    (println req)
    (println (format "request = %s" xml))
    (println (format "send to address %s" address))
    (send-request xml address)))


