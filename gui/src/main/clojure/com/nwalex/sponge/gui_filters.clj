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

(def lifecycle-mapping
     {com.nwalex.sponge.plugin.LifecyclePoint/BEFORE_REQUEST :request
      com.nwalex.sponge.plugin.LifecyclePoint/AFTER_RESPONSE :response})

(defn- lifecycle-store-mapping [session]
  {:request (:request-plugins session)
   :response (:response-plugins session)})

(defn display-exchange-filter [session server exchange key]
  (log/info "Ready to display exchange")
  (continue (model/add-exchange! session server exchange key)))

(defn display-non-replay-response-filter [session server exchange key]
  (if (not (:replayed exchange))
    (continue (model/add-exchange! session server exchange key))
    (continue exchange)))

(defn replay-filter [session server exchange key]
  (let [response (model/replay-response session exchange)]
    (if response
      (return (assoc exchange
                :response response
                :replayed true))
      (continue exchange))))

(defn fail-filter [session server exchange key]
  (abort exchange))

(defn- request-filter-map [session]
  {com.nwalex.sponge.gui.SpongeGUIController/FORWARD_ALL
   [(partial display-exchange-filter session)]
   
   com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FAIL
   [(partial replay-filter session) (partial fail-filter session)]
   
   com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FORWARD
   [(partial replay-filter session) (partial display-exchange-filter session)]
   })

(defn- response-filter-map [session]
  {com.nwalex.sponge.gui.SpongeGUIController/FORWARD_ALL
   [(partial display-exchange-filter session)]
   
   com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FAIL
   [(partial display-non-replay-response-filter session)]
   
   com.nwalex.sponge.gui.SpongeGUIController/REPLAY_OR_FORWARD
   [(partial display-non-replay-response-filter session)]})

(defn- get-plugin-filters
  "phase is either :request or :response"
  [session phase]
  (vec (vals (deref ((lifecycle-store-mapping session) phase)))))

;; note plugin filters happen before others on request
(defn get-request-filters-for-mode [session mode]
  (let [request-filters (vec (concat
                              (get-plugin-filters session :request)
                              ((request-filter-map session) mode)))]
    (log/info (format "request-filters-for-mode %s are: %s" mode request-filters))
    request-filters))

;; note plugin fitlers happen after others on response
(defn get-response-filters-for-mode [session mode]
  (let [response-filters (vec (concat
                               (get-plugin-filters session :response)
                               ((response-filter-map session) mode)))]
    (log/info (format "response-filters-for-mode %s are: %s" mode response-filters))
    response-filters))

(defn reload-filters [session]
  (log/info "Reloading filters...")
  (if (server/running? (state/current-server))
    (do
      (server/update-request-filters (state/current-server)
                                     (get-request-filters-for-mode
                                      session (state/get-mode)))
      (server/update-response-filters (state/current-server)
                                      (get-response-filters-for-mode
                                       session (state/get-mode))))))

(defn set-mode [session mode]  
  (state/set-mode! mode)
  (reload-filters session))

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

(defn- register-plugin [session phase plugin]
  (log/info (format "Register plugin %s for phasef %s" plugin phase))
  (let [plugin-store ((lifecycle-store-mapping session) phase)]
    (dosync
     (ref-set plugin-store (assoc @plugin-store (hash plugin)
                                  (partial plugin-filter plugin))))
    (log/info (format "Currently registered plugins for %s: %s" phase @plugin-store)))
  (.onEnabled plugin)
  (reload-filters session))

(defn- deregister-plugin [session phase plugin]
  (log/info (format "De-register plugin %s for phase %s" plugin phase))
  (let [plugin-store ((lifecycle-store-mapping session) phase)]
    (dosync
     (ref-set plugin-store (dissoc @plugin-store (hash plugin))))
    (log/info (format "Currently registered plugins for phase %s: %s"
                      phase @plugin-store)))
  (.onDisabled plugin)
  (reload-filters session))

(defn enable-plugin [session plugin]
  (let [lifecycle-point (lifecycle-mapping (.getLifecyclePoint plugin))]
    (try
     (register-plugin session lifecycle-point plugin)
     (catch Exception ex
       (deregister-plugin session lifecycle-point plugin)))))

(defn disable-plugin [session plugin]
  (let [lifecycle-point (lifecycle-mapping (.getLifecyclePoint plugin))]
    (deregister-plugin session lifecycle-point plugin)))

(defn make-plugin-controller [session]
  (let [controller (proxy [com.nwalex.sponge.gui.plugins.PluginController] []
                     (pluginEnabled [plugin] (enable-plugin session plugin))
                     (pluginDisabled [plugin] (disable-plugin session plugin))
                     (getPluginManager []
                                       (if-not @(:plugin-manager session)
                                         (dosync
                                          (ref-set
                                           (:plugin-manager session)
                                           (com.nwalex.sponge.gui.plugins.PluginManager.
                                            @(:plugin-controller session)))))
                                       @(:plugin-manager session)))]
    (dosync
     (ref-set (:plugin-controller session) controller))
    controller))
