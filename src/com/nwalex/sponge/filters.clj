(ns com.nwalex.sponge.filters
  (:require
   [clojure.contrib.logging :as log]))

(defn logging-filter
  "Simply logs r to stdout"
  [server r]
  (log/info r)
  {:continue r})
