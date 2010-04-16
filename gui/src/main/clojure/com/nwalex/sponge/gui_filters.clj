; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.gui-filters
  (:require
   [com.nwalex.sponge.gui-state :as state]
   [com.nwalex.sponge.table-model :as model]
   [com.nwalex.sponge.plugins :as plugins])
  (:use
   [com.nwalex.sponge.filters :only [continue return abort]]))

(defn display-exchange-filter [server exchange key]
  (continue (model/add-exchange! server exchange key)))

(defn display-non-replay-response-filter [server exchange key]
  (if (not (:replayed exchange))
    (continue (model/add-exchange! server exchange key))
    (continue exchange)))

(defn replay-filter [server exchange key]
  (let [response (model/replay-response exchange)]
    (if response
      (return (assoc exchange
                :response response
                :replayed true))
      (continue exchange))))

(defn fail-filter [server exchange key]
  (abort exchange))

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

;; note plugin filters happen before others on request
(defn get-request-filters-for-mode [mode]
  (vec (concat (plugins/get-plugin-filters :request) (request-filter-map mode))))

;; note plugin fitlers happen after others on response
(defn get-response-filters-for-mode [mode]
  (vec (concat (response-filter-map mode) (plugins/get-plugin-filters :response))))
