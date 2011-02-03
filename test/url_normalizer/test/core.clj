(ns url-normalizer.test.core
  (:refer-clojure :exclude (resolve))
  (:use
    [url-normalizer.core]
    [url-normalizer.test.util]
    [clojure.test])
  (:import
    [java.net URL URI]))

(refer-private 'url-normalizer.core)

(defn- as-uri-map
  [h]
  (apply merge
    (cons {} (map #(vector (as-uri (first %)) (as-uri (second %))) h))))

(def
  ^{:doc
    "Tests from RFC3986: Section 5.3.  The keys should resolve against
    http://a/b/c/d;p?q to the values.

    See <http://www.ietf.org/rfc/rfc3986.txt>"}
  rfc3986-normal-tests
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
  ^{:doc
    "Tests from RFC3986 Section 5.4.2.  The keys should resolve against
    http://a/b/c/d;p?q to the values.

    See <http://www.ietf.org/rfc/rfc3986.txt>"}
  rfc3986-abnormal-tests
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

(def
  ^{:doc
    "Pace tests.

    See <http://www.intertwingly.net/wiki/pie/PaceCanonicalIds>"}
  pace-tests
  (as-uri-map
    {"http://:@example.com/" "http://example.com/"
     "http://@example.com/" "http://example.com/"
     "http://example.com" "http://example.com/"
     "HTTP://example.com/" "http://example.com/"
     "http://EXAMPLE.COM/" "http://example.com/"
     "http://example.com/%7Ejane" "http://example.com/~jane"
     "http://example.com/?q=%C3%87" "http://example.com/?q=%C3%87"
     "http://example.com/?q=%E2%85%A0" "http://example.com/?q=%E2%85%A0"
     "http://example.com/?q=%5C" "http://example.com/?q=%5C"
     "http://example.com/a/../a/b" "http://example.com/a/b"
     "http://example.com/a/./b" "http://example.com/a/b"
     "http://example.com:80/" "http://example.com/"
     "http://example.com/" "http://example.com/"
     "http://example.com/~jane" "http://example.com/~jane"
     "http://example.com/a/b" "http://example.com/a/b"
     "http://example.com:8080/" "http://example.com:8080/"
     "http://user:password@example.com/" "http://user:password@example.com/"}))

(def
  ^{:doc
    "Tests from RFC2396.

    See <http://labs.apache.org/webarch/uri/rev-2002/rfc2396bis.html>"}
  rfc2396bis-tests
  (as-uri-map
    {"http://www.ietf.org/rfc/rfc2396.txt" "http://www.ietf.org/rfc/rfc2396.txt"
     "telnet://192.0.2.16:80/" "telnet://192.0.2.16:80/"
     "http://127.0.0.1/" "http://127.0.0.1/"
     "http://127.0.0.1:80/" "http://127.0.0.1/"
     "http://example.com:081/" "http://example.com:81/"
     "http://example.com?q=foo" "http://example.com/?q=foo"
     "http://example.com/?q=foo" "http://example.com/?q=foo"}))

(deftest test-reference-resolution
  (let [base (as-uri "http://a/b/c/d;p?q")]
    (doseq [[original resolved] rfc3986-normal-tests]
      (is (equal? (resolve base original) resolved)))
    (doseq [[original resolved] rfc3986-abnormal-tests]
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
  (is (equal? (normalize "example://a/b/c/%7Bfoo%7D")
              (normalize "eXAMPLE://a/./b/../b/%63/%7bfoo%7d"))))

(deftest
  ^{:doc "Tests from RFC3986: 6.2.2.1.  Case Normalization."}
  test-case-normalization
  (is (equal? (normalize "HTTP://www.EXAMPLE.com/")
              (normalize "http://www.example.com/")))
  (is (equal? (normalize "http://www.example.com/%7B")
              (normalize "http://www.example.com/%7b"))))

(deftest
  ^{:doc
    "Tests for RFC3986: 6.2.2.2.  Percent-Encoding Normalization.

    Do not percent encode unreserved characters.  When encountered, they
    should be decoded."}
  test-percent-encoding-normalization
  (is (equal? (normalize "http://example.com/~azAZ09-._")
              (normalize "http://example.com/%7E%61%7A%41%5A%30%39%2D%2E%5F"))))


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
    (is (equal? expected (normalize "http://example.com")))
    (is (equal? expected (normalize "http://example.com/")))
    (is (equal? expected (normalize "http://example.com:/")))
    (is (equal? expected (normalize "http://example.com:80/")))
    (is (not (equal? expected (normalize "http://www.example.com/?"))))))

(deftest test-normalize-user-info-part
  (letfn [(f ([uri] (f uri *context*))
             ([uri ctx] (normalize-user-info-part
                          (as-uri uri)
                          (merge *context* ctx))))]
    (is (= (f "http://user@example.com") "user@"))
    (is (= (f "http://user:password@example.com") "user:password@"))
    (is (= (f "http://@example.com") "@"))
    (is (nil? (f "http://example.com")))
    (is (nil? (f "http://@example.com" {:remove-empty-user-info? true})))
    (is (nil? (f "http://:@example.com" {:remove-empty-user-info? true})))))

(deftest test-normalize-host-part
  (letfn [(f ([uri] (f uri *context*))
             ([uri ctx] (normalize-host-part
                          (as-uri uri)
                          (merge *context* ctx))))]
    (is (= (f "http://WWW.EXAMPLE.COM") "www.example.com"))
    (is (= (f "http://www.example.com.") "www.example.com."))
    (is (= (f "http://www.example.com." {:remove-trailing-dot-in-host? true})
           "www.example.com"))
    (is (nil? (f "/")))
    (is (nil? (f "/" {:remove-trailing-dot-in-host? true})))))

(deftest test-normalize-port-part
  (letfn [(f ([uri] (f uri *context*))
             ([uri ctx] (normalize-port-part
                          (as-uri uri)
                          (merge *context* ctx))))]
    (is (= (f "http://example.com:8080") ":8080"))
    (is (= (f "http://example.com:8080" {:remove-default-port? true}) ":8080"))
    (is (nil? (f "http://example.com:80" {:remove-default-port? true})))))

(deftest test-normalize-path-part
  (letfn [(f ([uri] (f uri *context*))
             ([uri ctx] (normalize-path-part
                          (as-uri uri)
                          (merge *context* ctx))))]
    (is (= (f "http://example.com") "/"))
    (is (= (f "http://example.com/") "/"))
    (is (= (f "http://example.com/foo/bar/../baz") "/foo/baz"))
    (is (= (f "http://example.com/foo/bar/../baz/") "/foo/baz/"))
    (is (= (f "http://example.com/foo/../..") "/.."))))

(deftest test-normalize-fragment-part
  (letfn [(f ([uri] (f uri *context*))
             ([uri ctx] (normalize-fragment-part
                          (as-uri uri)
                          (merge *context* ctx))))]
    (is (= (f "http://example.com#foo") "#foo"))
    (is (= (f "http://example.com#") "#"))
    (is (nil? (f "http://example.com")))
    (is (nil? (f "http://example.com#foo" {:remove-fragment? true})))))
