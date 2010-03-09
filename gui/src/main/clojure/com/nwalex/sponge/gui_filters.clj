(ns com.nwalex.sponge.gui-filters
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]))

(defn display-exchange-filter [server exchange key]
  {:continue (model/add-exchange! server exchange key)})

(defn replay-filter [server exchange key]
  (let [response (model/replay-response exchange)]
    (if response
      {:return (assoc exchange :response response)}
      {:continue exchange})))

(defn fail-filter [server exchange key]
  (println "In fail-filter")
  {:abort exchange})

(def #^{:private true} request-filter-map
     {com.nwalex.sponge.gui.SpongeGUIController/FORWARD_ALL
      [display-exchange-filter]

      com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FAIL
      [display-exchange-filter replay-filter fail-filter]

      com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FORWARD
      [display-exchange-filter replay-filter]
      })

(defn get-request-filters-for-mode [mode]
  (println (format "Returning filters for mode %s" mode))
  (request-filter-map mode))
