(ns com.nwalex.sponge.http
  (:import
   [org.apache.commons.httpclient HttpClient]
   [org.apache.commons.httpclient.methods PostMethod StringRequestEntity])
  (:require   
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as duck]))

(defn- extract-header-value [val-str element]
  (let [name (.getName element)
        value (.getValue element)]
    (str val-str name (if value (format "=%s;" value) ";"))))

(defn- convert-content-type-header
  "Convert the headers to the map that Ring expects"
  [post]
  (let [content-type-header (.getResponseHeader post "content-type")]
    (reduce extract-header-value "" (.getElements content-type-header))))

(defn send-request
  "Creates a PostMethod setting the body to text, and sends to address"
  [text address]
  (let [client (HttpClient.)
        post (PostMethod. address)]
    (.setRequestEntity post
                       (StringRequestEntity. text "text/xml" "utf-8"))
    (try
     (do
       (.executeMethod client post)
       {:status (.getStatusCode post)
        :headers {"Content-Type" (convert-content-type-header post)}
        :body (duck/slurp* (.getResponseBodyAsStream post))})
     (finally
      (.releaseConnection post)))))

(defn forward-request
  "Forward the soap request on to the configured host / port"
  [target req]
  (let [xml (:body req) 
        address (format "%s%s" target (:uri req))]
    (log/info (format "Sending request to: %s" address))
    (send-request xml address)))

