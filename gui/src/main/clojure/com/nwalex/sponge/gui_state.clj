; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.gui-state
  (:require
   [com.nwalex.sponge.server :as server]
   [clojure.contrib.logging :as log]))

(defn gui [session] @(:gui-frame-store session))

(defn get-mode [session]
  @(:mode session))

(defn- set-title [session]
  (.setTitle
   (gui session)
   (format "Sponge  [%s]  [http://localhost:%s --> %s]  [%s]  [%s]"
           (if (server/running? @(:current-server-store session)) "Running" "Stopped")
           @(:port-store session) @(:target-store session) (get-mode session)
           (if @(:repl-running session) "REPL" ""))))

(defn set-config! [session port target]
  (log/info (format "Setting server config to: port = %s, target = %s" port target))
  ;; force a quick exception if the values are invalid
  (java.net.URL. target)     
  (dosync
   (ref-set (:port-store session) (if (integer? port) port (Integer/parseInt port)))
   (ref-set (:target-store session) target)
   (set-title session)))

(defn set-mode! [session new-mode]
  (log/info (format "Setting mode to \"%s\"" new-mode))
  (compare-and-set! (:mode session) @(:mode session) new-mode)
  (set-title session))

(defn load-from-persistence-map! [session persistence-map]
  (set-config! session (:port persistence-map) (:target persistence-map))
  (set-mode! session (:mode persistence-map)))

(defn get-persistence-map [session]
  {:port @(:port-store session) :target
   @(:target-store session) :mode @(:mode session)})

(defn set-gui! [session gui]
  (let [gui-frame-store (:gui-frame-store session)]
    (compare-and-set! gui-frame-store @gui-frame-store gui)
    (set-title session)
    gui))

(defn set-current-server! [session server]
  (compare-and-set! (:current-server-store session)
                    @(:current-server-store session) server)
  (set-title session))

(defn current-server [session]
  @(:current-server-store session))

(defn config [session]
  {:port @(:port-store session) :target @(:target-store session)})

(defn repl-started! [session]
  (reset! (:repl-running session) true)
  (set-title session))
