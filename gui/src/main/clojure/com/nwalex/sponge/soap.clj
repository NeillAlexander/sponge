(ns com.nwalex.sponge.soap
  (:require
   [clojure.zip :as zip]
   [clojure.xml :as xml]))

;; this is a bit hacky and may break!! Relying on the fact that
;; always have xmlns in the body and that the soap method is the
;; name of the first child of the body. Otherwise will need smarter
;; xml parsing here
;; Basically, I don't understand the zipper functions yet
;; TODO: tidy this up
(defn parse-soap [request]
  (let [zipped (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream.
                                        (.getBytes (:body request)))))]
    {:namespace (:xmlns (:attrs
                           (first (:content (first (:content (first zipped)))))))
     :soap-method (.substring (str
                            (:tag (first (:content
                                          (first (:content (first zipped))))))) 1)}))
