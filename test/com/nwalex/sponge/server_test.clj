(ns com.nwalex.sponge.server-test
  (:use
   [clojure.test])
  (:require
   [com.nwalex.sponge.server :as server]))

(deftest make-server-test-no-handlers
  (let [server (server/make-server 8747 "addr")]
    (is (= 8747 (:port server)))
    (is (= "addr" (:target server)))
    (is (nil? (:request-handlers server)))
    (is (nil? (:response-handlers server)))))

(deftest make-server-test-with-handlers
  (let [server (server/make-server 8747 "addr"
                                   :request-handlers '[a b c]
                                   :response-handlers '[d e f])]
    (is (= 8747 (:port server)))
    (is (= "addr" (:target server)))
    (is (not (nil? (:request-handlers server))))
    (is (not (nil? (:response-handlers server))))))
