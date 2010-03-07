(ns com.nwalex.sponge.gui-filters
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]))

(defn display-exchange-filter [server exchange key]
  {:continue (model/add-exchange! server exchange key)})
