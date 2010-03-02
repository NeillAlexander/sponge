(ns com.nwalex.sponge.datastore)

(defn make-datastore []
  (ref []))

(defn add-exchange [datastore exchange]
  (dosync (commute datastore conj exchange)))

(defn get-exchanges [datastore]
  (deref datastore))
