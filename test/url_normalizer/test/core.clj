(ns url-normalizer.test.core
  (:refer-clojure :exclude (resolve))
  (:use
    [url-normalizer.core]
    [clojure.test])
  (:import
    [java.net URL URI]))

(defn as-uri-map
  [h]
  (apply merge
    (cons {} (map #(vector (as-uri (first %)) (as-uri (second %))) h))))

(defn equal?
  "Returns true if the ASCII string versions of URIs are equal.  This is
  different from #'url-normalizer.core/equivalent? as two equivalent URIs
  may not have the same ASCII string representation.

  For example:

    http://example.com/%7b
    http://example.com/%7B"
  [a b]
  (= (.toASCIIString a) (.toASCIIString b)))

(def
  ^{:doc "Tests from RFC3986: Section 5.3."}
  normal-reference-resolution-examples
  (as-uri-map
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
  ^{:doc "Tests from RFC3986 Section 5.4.2."}
  abnormal-reference-resolution-examples
  (as-uri-map
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
  (let [base (as-uri "http://a/b/c/d;p?q")]
    (doseq [[original resolved] normal-reference-resolution-examples]
      (is (equal? (resolve base original) resolved)))
    (doseq [[original resolved] abnormal-reference-resolution-examples]
      (is (equal? (resolve base original) resolved)))))

(deftest
  ^{:doc "See <http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>"}
  test-fixes-java-bug-4708535
  (let [expected (as-uri "http://example.org/dir/file#foo")]
    (is (equal? expected (resolve (as-uri "http://example.org/dir/file")
                                  (as-uri "#foo"))))
    (is (equal? expected (resolve (as-uri "http://example.org/dir/file#frag")
                                  (as-uri "#foo"))))
  (let [expected (as-uri "http://example.org/dir/file")]
    (is (equal? expected (resolve (as-uri "http://example.org/dir/file")
                                  (as-uri ""))))
    (is (equal? expected (resolve (as-uri "http://example.org/dir/file#frag")
                                  (as-uri "")))))))

(deftest
  ^{:doc "Tests from RFC3986: 6.2.2.  Syntax-Based Normalization."}
  test-syntax-based-normalization
  (is (equal? (normalize (as-uri "example://a/b/c/%7Bfoo%7D"))
              (normalize (as-uri "eXAMPLE://a/./b/../b/%63/%7bfoo%7d")))))

(deftest
  ^{:doc "Tests from RFC3986: 6.2.2.1.  Case Normalization."}
  test-case-normalization
  (is (equal? (normalize (as-uri "HTTP://www.EXAMPLE.com/"))
              (normalize (as-uri "http://www.example.com/"))))
  (is (equal? (normalize (as-uri "http://www.example.com/%7B"))
              (normalize (as-uri "http://www.example.com/%7b")))))

(deftest
  ^{:doc
    "Tests for RFC3986: 6.2.2.2.  Percent-Encoding Normalization.

    Do not percent encode unreserved characters.  When encountered, they
    should be decoded."}
  test-percent-encoding-normalization
  (is (equal? (normalize (as-uri "http://example.com/~azAZ09-._"))
              (normalize (as-uri "http://example.com/%7E%41%5A%61%7A%30%39%2D%2E%5F")))))


(comment "From 6.2.2.3.  Path Segment Normalization")

(deftest
  ^{:doc
    "Tests from RFC3986: 6.2.3.  Scheme-Based Normalization.

     The preceding section describes additional normalizations for other schemes.
     For example, the following are equivalent URIs:

     mailto:Joe@example.com
     mailto:Joe@Example.com

     Being a URL normalizer, we will ignore these."}
  test-scheme-based-normalization
  (let [expected (as-uri "http://example.com/")]
    (is (equal? expected (normalize (as-uri "http://example.com"))))
    (is (equal? expected (normalize (as-uri "http://example.com/"))))
    (is (equal? expected (normalize (as-uri "http://example.com:/"))))
    (is (equal? expected (normalize (as-uri "http://example.com:80/"))))
    (is (not (equal? expected
                     (normalize (as-uri "http://www.example.com/?")))))))
