; Copyright (c) Neill Alexander. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software

(ns com.nwalex.sponge.soap
  (:require
   [clojure.zip :as zip]
   [clojure.xml :as xml]
   [clojure.contrib.logging :as log]))

;; this is a bit hacky and may break!! Relying on the fact that
;; always have xmlns in the body and that the soap method is the
;; name of the first child of the body. Otherwise will need smarter
;; xml parsing here
;; Basically, I don't understand the zipper functions yet
;; TODO: tidy this up
(defn parse-soap [request]
  (try
   (let [zipped (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream.
                                         (.getBytes (:body request)))))]
     {:namespace (:xmlns (:attrs
                          (first (:content (first (:content (first zipped)))))))
      :soap-method (.substring (str
                                (:tag (first (:content
                                              (first (:content
                                                      (first zipped))))))) 1)})
   (catch Exception ex
     (log/error "Error parsing soap" ex)
     {:namespace "Unknown" :soap-method "Unknown"})))
