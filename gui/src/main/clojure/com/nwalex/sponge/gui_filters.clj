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
   [com.nwalex.sponge.server :as server]
   [clojure.contrib.logging :as log])
  (:use
   [com.nwalex.sponge.filters :only [continue return abort]]))

(def plugin-manager (ref nil))

(def request-plugins (ref {}))
(def response-plugins (ref {}))

(def lifecycle-mapping
     {com.nwalex.sponge.plugin.LifecyclePoint/BEFORE_REQUEST :request
      com.nwalex.sponge.plugin.LifecyclePoint/AFTER_RESPONSE :response})

(def lifecycle-store-mapping
     {:request request-plugins
      :response response-plugins})

(defn display-exchange-filter [server exchange key]
  (log/info "Ready to display exchange")
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

(defn get-plugin-filters
  "phase is either :request or :response"
  [phase]
  (vec (vals (deref (lifecycle-store-mapping phase)))))

;; note plugin filters happen before others on request
(defn get-request-filters-for-mode [mode]
  (let [request-filters (vec (concat
                              (get-plugin-filters :request)
                              (request-filter-map mode)))]
    (log/info (format "request-filters-for-mode %s are: %s" mode request-filters))
    request-filters))

;; note plugin fitlers happen after others on response
(defn get-response-filters-for-mode [mode]
  (let [response-filters (vec (concat
                               (get-plugin-filters :response)
                               (response-filter-map mode)))]
    (log/info (format "response-filters-for-mode %s are: %s" mode response-filters))
    response-filters))

(defn reload-filters []
  (log/info "Reloading filters...")
  (if (server/running? (state/current-server))
    (do
      (server/update-request-filters (state/current-server)
                                     (get-request-filters-for-mode
                                      (state/get-mode)))
      (server/update-response-filters (state/current-server)
                                      (get-response-filters-for-mode
                                       (state/get-mode))))))

(defn set-mode [mode]  
  (state/set-mode! mode)
  (reload-filters))

(defn- build-response [exchange phase key body]
  (log/info (format "in build-response: %s" phase))
  (let [updated-exchange (assoc-in exchange [phase :body] body)]
    {key updated-exchange}))
  

(defn- response-builder [exchange phase]
  (proxy [com.nwalex.sponge.plugin.PluginResponseBuilder] []
    (buildContinueResponse [data] (build-response exchange phase :continue data))
    (buildReturnResponse [data] (build-response exchange phase :return data))
    (buildAbortResponse [data] (build-response exchange phase :return data))))

(defn- plugin-context [builder]
     (proxy [com.nwalex.sponge.plugin.PluginContext] []
       (getResponseBuilder [] builder)
       (isValidResponse [response]                        
                        (or (contains? response :return)
                            (contains? response :abort)
                            (contains? response :continue)))))

(defn- plugin-filter [plugin server exchange phase]
  (let [body (:body (exchange phase))
        builder (partial response-builder exchange phase)
        context (partial plugin-context (builder))
        response (.execute plugin body (context))]
    response))

(defn- register-plugin [phase plugin]
  (log/info (format "Register plugin %s for phasef %s" plugin phase))
  (let [plugin-store (lifecycle-store-mapping phase)]
    (dosync
     (ref-set plugin-store (assoc @plugin-store (hash plugin)
                                  (partial plugin-filter plugin))))
    (log/info (format "Currently registered plugins for %s: %s" phase @plugin-store)))
  (.onEnabled plugin)
  (reload-filters))

(defn- deregister-plugin [phase plugin]
  (log/info (format "De-register plugin %s for phase %s" plugin phase))
  (let [plugin-store (lifecycle-store-mapping phase)]
    (dosync
     (ref-set plugin-store (dissoc @plugin-store (hash plugin))))
    (log/info (format "Currently registered plugins for phase %s: %s"
                      phase @plugin-store)))
  (.onDisabled plugin)
  (reload-filters))

(defn enable-plugin [plugin]
  (let [lifecycle-point (lifecycle-mapping (.getLifecyclePoint plugin))]
    (try
     (register-plugin lifecycle-point plugin)
     (catch Exception ex
       (deregister-plugin lifecycle-point plugin)))))

(defn disable-plugin [plugin]
  (let [lifecycle-point (lifecycle-mapping (.getLifecyclePoint plugin))]
    (deregister-plugin lifecycle-point plugin)))

(def plugin-controller
     (proxy [com.nwalex.sponge.gui.plugins.PluginController] []
       (pluginEnabled [plugin] (enable-plugin plugin))
       (pluginDisabled [plugin] (disable-plugin plugin))
       (getPluginManager []
                         (if-not @plugin-manager
                           (dosync
                            (ref-set plugin-manager
                                     (com.nwalex.sponge.gui.plugins.PluginManager.
                                      plugin-controller))))
                         @plugin-manager)))
