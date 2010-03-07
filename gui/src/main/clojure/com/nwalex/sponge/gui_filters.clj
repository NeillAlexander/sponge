(ns com.nwalex.sponge.gui-filters
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [clojure.contrib.logging :as log]))

(defn display-exchange-filter [server exchange key]
  (let [updated-exchange (state/add-exchange! server exchange key)]
    (log/info (format "Updated exchange: %s" updated-exchange))
    {:continue updated-exchange}))
