(ns com.nwalex.sponge.gui
  (:require
   [com.nwalex.sponge.server :as server]))

;; TODO: is this the best way?
;; should be able to start server without calling main?
;; or doesn't it really matter??
;; It WILL matter - need to be able to configure additional
;; filters. need to think of the best way to do this
(defn- start-server []
  (server/start (server/make-server
                        8139
                        "http://localhost:8140")))

(defn -main [& args]
  (let [controller (proxy [com.nwalex.sponge.gui.SpongeGUIController] []
                     (startServer [port target]
                                  (start-server)))
        gui (com.nwalex.sponge.gui.SpongeGUI. controller)]
    (.setVisible gui true)))
