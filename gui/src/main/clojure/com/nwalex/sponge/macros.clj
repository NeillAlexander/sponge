(ns com.nwalex.sponge.macros
  (:require
   [clojure.contrib.logging :as log]))

(defmacro defn-dirty
  "Define a function that will automatically mark the map
supplied as the first argument as dirty i.e. :dirty true.
Assumes that there is a key called :dirty with a ref as
value in the first function argument"
  [name args & body]
  `(defn ~name ~args
     (log/info "Setting :dirty flag to true...")
     (dosync (ref-set (:dirty (first ~args)) true))
     (do ~@body)))
