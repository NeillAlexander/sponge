(ns com.nwalex.sponge.gui-filters
  (:require
   [com.nwalex.sponge.gui-state :as state]))

(defn display-exchange-filter [server exchange key]
  (state/add-exchange! server exchange key)
  {:continue exchange})
