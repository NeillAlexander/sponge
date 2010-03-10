(ns com.nwalex.sponge.gui-filters
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]))

(defn display-exchange-filter [server exchange key]
  {:continue (model/add-exchange! server exchange key)})

(defn display-non-replay-response-filter [server exchange key]
  (if (not (:replayed exchange))
    {:continue (model/add-exchange! server exchange key)}
    {:continue exchange}))

(defn replay-filter [server exchange key]
  (let [response (model/replay-response exchange)]
    (if response
      {:return (assoc exchange
                 :response response
                 :replayed true)}
      {:continue exchange})))

(defn fail-filter [server exchange key]
  {:abort exchange})

(def #^{:private true} request-filter-map
     {com.nwalex.sponge.gui.SpongeGUIController/FORWARD_ALL
      [display-exchange-filter]

      com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FAIL
      [replay-filter fail-filter]

      com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FORWARD
      [replay-filter display-exchange-filter]
      })

(def #^{:private true} response-filter-map
     {com.nwalex.sponge.gui.SpongeGUIController/FORWARD_ALL
      [display-exchange-filter]

      com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FAIL
      [display-non-replay-response-filter]

      com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FORWARD
      [display-non-replay-response-filter]
      })

(defn get-request-filters-for-mode [mode]
  (request-filter-map mode))

(defn get-response-filters-for-mode [mode]
  (response-filter-map mode))
