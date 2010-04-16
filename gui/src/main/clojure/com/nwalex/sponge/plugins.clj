(ns com.nwalex.sponge.plugins)

(def plugin-manager (ref nil))

(def request-plugins (ref {}))
(def response-plugins (ref {}))

(def lifecycle-mapping
     {com.nwalex.sponge.plugin.LifecyclePoint/BEFORE_REQUEST :request
      com.nwalex.sponge.plugin.LifecyclePoint/AFTER_RESPONSE :response})

(def lifecycle-store-mapping
     {:request request-plugins
      :response response-plugins})

(defn- register-plugin [phase plugin]
  (println (format "Register plugin %s for phasef %s" plugin phase))
  (let [plugin-store (lifecycle-store-mapping phase)]
    (dosync
     (ref-set plugin-store (assoc @plugin-store (hash plugin) plugin)))
    (println @plugin-store)))

(defn- deregister-plugin [phase plugin]
  (println (format "De-register plugin %s for phasef %s" plugin phase))
  (let [plugin-store (lifecycle-store-mapping phase)]
    (dosync
     (ref-set plugin-store (dissoc @plugin-store (hash plugin))))
    (println @plugin-store)))

(defn enable-plugin [plugin]
  (println (format "Ready to enable plugin %s" plugin))
  (let [lifecycle-point (lifecycle-mapping (.getLifecyclePoint plugin))]
    (register-plugin lifecycle-point plugin)))

(defn disable-plugin [plugin]
  (println (format "Ready to disable plugin %s" plugin))
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

(defn plugin-filter [plugin server exchange key]
  (println (format "Dummy plugin filter executed for %s" key))
  {:continue exchange})

(defn get-plugin-filters
  "phase is either :request or :response"
  [phase]
  [(partial plugin-filter nil)])
