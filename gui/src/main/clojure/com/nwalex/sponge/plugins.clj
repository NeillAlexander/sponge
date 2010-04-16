(ns com.nwalex.sponge.plugins
  (:require
   [clojure.contrib.logging :as log]))

(def plugin-manager (ref nil))

(def request-plugins (ref {}))
(def response-plugins (ref {}))

(def lifecycle-mapping
     {com.nwalex.sponge.plugin.LifecyclePoint/BEFORE_REQUEST :request
      com.nwalex.sponge.plugin.LifecyclePoint/AFTER_RESPONSE :response})

(def lifecycle-store-mapping
     {:request request-plugins
      :response response-plugins})

(defn- build-response [exchange key data]
  {key (assoc exchange key data)})

(defn- response-builder [exchange phase]
  (proxy [com.nwalex.sponge.plugin.PluginResponseBuilder] []
    (buildContinueResponse [data] (build-response exchange :continue data))
    (buildReturnResponse [data] (build-response exchange :return data))
    (buildAbortResponse [data] (build-response exchange :return data))))

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
        response (.execute plugin body (context))
        response-key (first (keys response))
        updated-phase
        {response-key (assoc (exchange phase) :body (response response-key))}
        updated-exchange (assoc exchange phase updated-phase)]
    ;; we have {:continue "request body here"}
    ;; need to update the exchange body and use
    ;; the key for the response
    (println (format "Response key: %s" response-key))
    (println (format "Exchange before: %s\nExchange after: %s" exchange updated-exchange))
   updated-exchange))

(defn- register-plugin [phase plugin]
  (log/info (format "Register plugin %s for phasef %s" plugin phase))
  (let [plugin-store (lifecycle-store-mapping phase)]
    (dosync
     (ref-set plugin-store (assoc @plugin-store (hash plugin)
                                  (partial plugin-filter plugin))))
    (println @plugin-store)))

(defn- deregister-plugin [phase plugin]
  (log/info (format "De-register plugin %s for phasef %s" plugin phase))
  (let [plugin-store (lifecycle-store-mapping phase)]
    (dosync
     (ref-set plugin-store (dissoc @plugin-store (hash plugin))))
    (println @plugin-store)))

(defn enable-plugin [plugin]
  (let [lifecycle-point (lifecycle-mapping (.getLifecyclePoint plugin))]
    (register-plugin lifecycle-point plugin)))

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

(defn get-plugin-filters
  "phase is either :request or :response"
  [phase]
  (vec (vals (deref (lifecycle-store-mapping phase)))))
