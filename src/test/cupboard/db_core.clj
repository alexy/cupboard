(ns test.cupboard.db-core
  (:use [clojure.contrib test-is profile])
  (:use [cupboard utils db-core]))



;;; ----------------------------------------------------------------------
;;; fixtures
;;; ----------------------------------------------------------------------

(declare *db-path* *db-env* *db*)


(defn fixture-db-path [f]
  (binding [*db-path* (.getAbsolutePath (make-temp-dir))]
    (f)
    (rmdir-recursive *db-path*)))


(defn fixture-db-env [f]
  (binding [*db-env* (db-env-open *db-path* :allow-create true)]
    (f)
    (db-env-close *db-env*)))


(defn fixture-db [f]
  (binding [*db* (db-open *db-env* "db" :allow-create true)]
    (f)
    (db-close *db*)))


(use-fixtures :each fixture-db-path fixture-db-env fixture-db)



;;; ----------------------------------------------------------------------
;;; tests
;;; ----------------------------------------------------------------------

(deftest basics
  (db-put *db* "one" 1)
  (is (= (db-get *db* "one") ["one" 1]))
  (dotimes [i 100] (db-put *db* (str i) i))
  (is (= (db-get *db* "50") ["50" 50])))


(deftest types
  (let [now (java.util.Date.)]
    (db-put *db* "boolean" true)
    (db-put *db* "char"    \c)
    (db-put *db* "byte"    (byte 1))
    (db-put *db* "short"   (short 1))
    (db-put *db* "int"     (int 1))
    (db-put *db* "long"    (long 1))
    (db-put *db* "bigint"  (bigint 1))
    (db-put *db* "ratio"   (/ 1 2))
    (db-put *db* "double"  1.0)
    (db-put *db* "string"  "hello world")
    (db-put *db* "date"    now)
    (db-put *db* "keyword" :one)
    (db-put *db* "symbol"  'one)
    (db-put *db* "list"    (list 1 2 3))
    (db-put *db* "vector"  [1 2 3])
    (db-put *db* "map"     {:one 1 :two 2 :three 3})
    (db-put *db* "set"     #{:one 2 'three})
    (is (= (db-get *db* "boolean") ["boolean" true]))
    (is (= (db-get *db* "char")    ["char" \c]))
    (is (= (db-get *db* "byte")    ["byte" (byte 1)]))
    (is (= (db-get *db* "short")   ["short" (short 1)]))
    (is (= (db-get *db* "int")     ["int" (int 1)]))
    (is (= (db-get *db* "long")    ["long" (long 1)]))
    (is (= (db-get *db* "bigint")  ["bigint" (bigint 1)]))
    (is (= (db-get *db* "ratio")   ["ratio" (/ 1 2)]))
    (is (= (db-get *db* "double")  ["double" 1.0]))
    (is (= (db-get *db* "string")  ["string" "hello world"]))
    (is (= (db-get *db* "date")    ["date" now]))
    (is (= (db-get *db* "keyword") ["keyword" :one]))
    (is (= (db-get *db* "symbol")  ["symbol" 'one]))
    (is (= (db-get *db* "list")    ["list" (list 1 2 3)]))
    (is (= (db-get *db* "vector")  ["vector" [1 2 3]]))
    (is (= (db-get *db* "map")     ["map" {:one 1 :two 2 :three 3}]))
    (is (= (db-get *db* "set")     ["set" #{:one 2 'three}]))))


(deftest cursors []
  (let [data1 #{:one 1 :two 2 :three "three" :four "five six seven eight"}
        data2 "hello world"
        data3 3.3
        data4 [4]]
    (db-put *db* "a" data1)
    (db-put *db* "b" data2)
    (db-put *db* "c" data3)
    (db-put *db* "d" data4)
    (let [cur1 (db-cursor-open *db*)]
      (is (= (db-cursor-get cur1 "a")
             (list ["a" data1] ["b" data2] ["c" data3] ["d" data4])))
      (db-cursor-close cur1))))