(ns url-normalizer.test.core
  (:refer-clojure :exclude (resolve))
  (:use
    [url-normalizer.core]
    [clojure.test])
  (:import
    [java.net URL URI]))

(defn to-uri-map
  [h]
  (apply merge (cons {} (map #(vector (URI. (first %)) (URI. (second %))) h))))

(def
  ^{:doc "These tests are from RFC3986 Section 5.3."}
  normal-reference-resolution-examples
  (to-uri-map
    {"g:h" "g:h"
     "g" "http://a/b/c/g"
     "./g" "http://a/b/c/g"
     "g/" "http://a/b/c/g/"
     "/g" "http://a/g"
     "//g" "http://g"
     "?y" "http://a/b/c/d;p?y"
     "g?y" "http://a/b/c/g?y"
     "#s" "http://a/b/c/d;p?q#s"
     "g#s" "http://a/b/c/g#s"
     "g?y#s" "http://a/b/c/g?y#s"
     ";x" "http://a/b/c/;x"
     "g;x" "http://a/b/c/g;x"
     "g;x?y#s" "http://a/b/c/g;x?y#s"
     "" "http://a/b/c/d;p?q"
     "." "http://a/b/c/"
     "./" "http://a/b/c/"
     ".." "http://a/b/"
     "../" "http://a/b/"
     "../g" "http://a/b/g"
     "../.." "http://a/"
     "../../" "http://a/"
     "../../g" "http://a/g"}))
(def
  ^{:doc "These tests are from RFC3986 Section 5.4.2."}
  abnormal-reference-resolution-examples
  (to-uri-map
    {"../../../g" "http://a/g"
     "../../../../g" "http://a/g"
     "/./g" "http://a/g"
     "/../g" "http://a/g"
     "g." "http://a/b/c/g."
     ".g" "http://a/b/c/.g"
     "g.." "http://a/b/c/g.."
     "..g" "http://a/b/c/..g"
     "./../g" "http://a/b/g"
     "./g/." "http://a/b/c/g/"
     "g/./h" "http://a/b/c/g/h"
     "g/../h" "http://a/b/c/h"
     "g;x=1/./y" "http://a/b/c/g;x=1/y"
     "g;x=1/../y" "http://a/b/c/y"
     "g?y/./x" "http://a/b/c/g?y/./x"
     "g?y/../x" "http://a/b/c/g?y/../x"
     "g#s/./x" "http://a/b/c/g#s/./x"
     "g#s/../x" "http://a/b/c/g#s/../x"}))

(deftest test-reference-resolution
  (let [base (URI. "http://a/b/c/d;p?q")]
    (doseq [[original resolved] normal-reference-resolution-examples]
      (is (= (resolve base original) resolved)))
    (doseq [[original resolved] abnormal-reference-resolution-examples]
      (is (= (resolve base original) resolved)))))

(deftest
  ^{:doc "See <http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>"}
  test-fixes-java-bug-4708535
  (let [expected (URI. "http://example.org/dir/file#foo")]
    (is (= expected (resolve (URI. "http://example.org/dir/file")
                             (URI. "#foo"))))
    (is (= expected (resolve (URI. "http://example.org/dir/file#frag")
                             (URI. "#foo"))))
  (let [expected (URI. "http://example.org/dir/file")]
    (is (= expected (resolve (URI. "http://example.org/dir/file")
                             (URI. ""))))
    (is (= expected (resolve (URI. "http://example.org/dir/file#frag")
                             (URI. "")))))))

(comment "From 6.2.2.  Syntax-Based Normalization in RFC3986"
(deftest
  test-syntax-based-normalization
  (is (equivalent? (URI. "example://a/b/c/%7Bfoo%7D")
                   (URI. "eXAMPLE://a/./b/../b/%63/%7bfoo%7d")))))

(deftest
  ^{:doc "Tests from 6.2.2.1.  Case Normalization in RFC3986"}
  test-case-normalization
  (is (equivalent? (URI. "HTTP://www.EXAMPLE.com/")
                   (URI. "http://www.example.com/"))))

(comment "From 6.2.2.2.  Percent-Encoding Normalization")

(comment "From 6.2.2.3.  Path Segment Normalization")

(deftest
  ^{:doc "Tests from RFC3986: 6.2.3.  Scheme-Based Normalization.

         The preceding section describes additional normalizations for other schemes.
         For example, the following are equivalent URIs:

         mailto:Joe@example.com
         mailto:Joe@Example.com

         Being a URL normalizer, we will ignore these."}
  test-scheme-based-normalization
  (let [expected (URI. "http://example.com")]
    (is (equivalent? expected (URI. "http://example.com/")))
    (is (equivalent? expected (URI. "http://example.com/")))
    (is (equivalent? expected (URI. "http://example.com:/")))
    (is (equivalent? expected (URI. "http://example.com:80/")))
    (is (not (equivalent? expected (URI. "http://www.example.com/?"))))))
