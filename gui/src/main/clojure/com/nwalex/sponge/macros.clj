(ns com.nwalex.sponge.macros
  (:require
   [clojure.contrib.logging :as log]))

(defmacro defn-dirty
  "Define a function that will automatically mark the map
supplied as the first argument as dirty i.e. :dirty true"
  [name args & body]
  `(defn ~name ~args
     (dosync (ref-set (:dirty (first ~args)) true))
     (do ~@body)))
