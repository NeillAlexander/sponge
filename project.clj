(defproject com.nwalex/sponge "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [ring "0.1.1-SNAPSHOT"]
                 [swank-clojure "1.1.0"]
                 [leiningen/lein-swank "1.1.0"]
                 [commons-logging "1.1.1"]
                 [log4j "1.2.15"]]
  :namespaces (com.nwalex.sponge.core)
  :main com.nwalex.sponge.Sponge
  )
