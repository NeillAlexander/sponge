(ns com.nwalex.sponge.http
  (:require
   [clojure.contrib.duck-streams :as ds]
   [clojure.contrib.logging :as log]
   [clojure.contrib.http.agent :as http]))

(defn send-request
  "Sends a http request to address with text as the body"
  [text address]
  (http/string
   (http/http-agent address
                    :method "POST"
                    :body text
                    :headers {"Content-Type", "text/xml; charset=utf-8"})))

(defn forward-request
  "Forward the soap request on to the configured host / port"
  [target req]
  (let [xml (ds/slurp* (:body req)) 
        address (format "%s%s" target (:uri req))]
    (log/info (format "Sending request to: %s" address))
    (send-request xml address)))


