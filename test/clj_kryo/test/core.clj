(ns clj-kryo.test.core
  (:use clojure.test)
  (:require
   [clj-kryo.core :as kryo])
  (:import
   java.util.HashMap
   [java.io ByteArrayInputStream ByteArrayOutputStream]
   [com.esotericsoftware.kryo.io Output Input]
   clj_kryo.support.KryoSerializer))

(deftest write-object
  (is (< 0
         (let [bos (ByteArrayOutputStream.)]
           (with-open [out ^Output (kryo/make-output bos)]
             (kryo/write-object out (HashMap. {:foo 1 :bar 2})))
           (.size bos)))))

(defn kryo-round-trip [expr]
  (let [bos (ByteArrayOutputStream.)]
    (with-open [out ^Output (kryo/make-output bos)]
      (kryo/write-object out expr))
    (let [bis (ByteArrayInputStream. (.toByteArray bos))]
      (with-open [in ^Input (kryo/make-input bis)]
        (kryo/read-object in)))))

(deftest read-object
  (is (= 1 (kryo-round-trip 1)))
  (is (= "abc" (kryo-round-trip "abc")))
  (is (= 'abc (kryo-round-trip 'abc)))
  (is (= :abc (kryo-round-trip :abc)))
  (is (= ["foo"] (kryo-round-trip ["foo"])))
  (is (= '("foo") (kryo-round-trip '("foo"))))
  (is (= #{"foo"} (kryo-round-trip #{"foo"})))
  (is (= #{"foo" 1} (kryo-round-trip #{"foo" 1})))
  (is (= ["foo"] (kryo-round-trip (lazy-seq ["foo"]))))

  (let [m {:foo 1 :bar [2 3] :baz "four"}]
    (is (= m (kryo-round-trip m))))

  (let [m {32  {"int" 1 "string" "string"}
           65 {"foo" "bar"}}]
    (is (= m
           (let [bos (ByteArrayOutputStream.)]
             (with-open [out ^Output (kryo/make-output bos)]
               (kryo/write-object out m))
             (let [bis (ByteArrayInputStream. (.toByteArray bos))]
               (with-open [in ^Input (kryo/make-input bis)]
                 (into {} (kryo/read-object in)))))))))

(deftest kryo-serializer
  (let [m {:a 1}]
    (is (= m (KryoSerializer/read (KryoSerializer/write m))))))
