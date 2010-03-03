(ns com.nwalex.sponge.datastore)

(defn make-datastore [server]
  (assoc server :datastore (agent [])))

(defn add-exchange [server exchange]
  (send-off (:datastore server) conj exchange))

(defn get-exchanges [server]
  (deref (:datastore server)))
