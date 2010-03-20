(ns com.nwalex.sponge.test-exchange
  (:require
   [com.nwalex.sponge.exchange :as exchange])
  (:use
   [clojure.test]))

(deftest test-pretty-print-invalid-xml
  (is (= "test" (exchange/do-pretty-print "test"))))
