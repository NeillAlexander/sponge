(ns com.nwalex.sponge.plugins)

(def plugin-manager (ref nil))

(defn enable-plugin [plugin]
  (println (format "Ready to enable plugin %s" plugin)))

(defn disable-plugin [plugin]
  (println (format "Ready to disable plugin %s" plugin)))

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
