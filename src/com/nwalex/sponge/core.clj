(ns com.nwalex.sponge.core
  [:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :as reload]
   [clojure.contrib.duck-streams :as ds]
   ])

(defn- handle-request [req]
  (println (ds/slurp* (:body req)))
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World from Ring - reloaded!"})

(defn app [req]
  (handle-request req))

(def with-reload-app (reload/wrap-reload app '(com.nwalex.sponge.core))) 

(defn start
  "Start and return instance of server"
  []
  (jetty/run-jetty with-reload-app {:port 8080 :join false}))

(defn stop [server]
  (.stop server))
