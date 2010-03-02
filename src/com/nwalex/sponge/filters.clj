(ns com.nwalex.sponge.filters
  (:require
   [clojure.contrib.logging :as log]))

(defn logging-filter
  "Simply logs exchange to stdout"
  [server exchange key]
  (log/info (key exchange))
  {:continue exchange})
