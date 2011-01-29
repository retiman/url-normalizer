(ns url-normalizer.test.core
  (:refer-clojure :exclude (resolve))
  (:use
    [url-normalizer.core]
    [clojure.test])
  (:import
    [java.net URL URI]))

(defn to-uri-map
  [h]
  (apply merge (cons {} (map #(vector (URI. (first %)) (URI. (second %)))) h)))

(def
  ^{:doc "These tests are from RFC3986 Section 5.3."}
  normal-reference-resolution-examples
  (to-uri-map {"g:h" "g:h"
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
  (to-uri-map {"../../../g" "http://a/g"
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
      (is (= (resolve base origin) resolved)))
    (doseq [[original resolved] abnormal-reference-resolution-examples]
      (is (= (resolve base origin) resolved)))))

(comment "From 6.2.2.  Syntax-Based Normalization in RFC3986"
(deftest test-syntax-based-normalization
  (is (equivalent? (URI. "example://a/b/c/%7Bfoo%7D")
                   (URI. "eXAMPLE://a/./b/../b/%63/%7bfoo%7d")))))

(comment "From 6.2.2.1.  Case Normalization in RFC3986"
(deftest test-case-normalization
  (is (equivalent? (URI. "HTTP://www.EXAMPLE.com/")
                   (URI. "http://www.example.com/")))))

(comment "From 6.2.2.2.  Percent-Encoding Normalization")

(comment "From 6.2.2.3.  Path Segment Normalization")

(comment "From 6.2.3.  Scheme-Based Normalization"
(deftest test-scheme-based-normalization
  (is (equivalent? (URI. "mailto:Joe@example.com") (URI. "mailto:Joe@example.com")))
  (is (equivalent? (URI. "http://example.com") (URI. "http://example.com/")))
  (is (equivalent? (URI. "http://example.com") (URI. "http://example.com/")))
  (is (equivalent? (URI. "http://example.com") (URI. "http://example.com:/")))
  (is (equivalent? (URI. "http://example.com") (URI. "http://example.com:80/")))))

(def
  ^{:doc "URLs mapped to true normalize to themselves."}
  absolute-uris
  {"http://:@example.com/" false
   "http://@example.com/" false
   "http://example.com" false
   "HTTP://example.com/" false
   "http://EXAMPLE.COM/" false
   "http://example.com/%7Ejane" false
   "http://example.com/?q=%C3%87" true
   "http://example.com/?q=%E2%85%A0" true
   "http://example.com/?q=%5C" true
   "http://example.com/a/../a/b" false
   "http://example.com/a/./b" false
   "http://example.com:80/" false
   "http://example.com/" true
   "http://example.com/~jane" true
   "http://example.com/a/b" true
   "http://example.com:8080/" true
   "http://user:password@example.com/" true
   "http://www.ietf.org/rfc/rfc2396.txt" true
   "telnet://192.0.2.16:80/" true
   "http://127.0.0.1/" true
   "http://127.0.0.1:80/" false
   "http://example.com:081/" false
   "http://example.com?q=foo" false
   "http://example.com/?q=foo" true})

(def
  ^{:doc "Tests for relative URIs."}
  relative-uris
  {"/foo/bar/." "/foo/bar/"
   "/foo/bar/./" "/foo/bar/"
   "/foo/bar/.." "/foo/"
   "/foo/bar/../" "/foo/"
   "/foo/bar/../baz" "/foo/baz"
   "/foo/bar/../.." "/"
   "/foo/bar/../../" "/"
   "/foo/bar/../../baz" "/baz"
   "/foo/bar/../../../baz" "/baz" ;;was: "/../baz"
   "/foo/bar/../../../../baz" "/baz"
   "/./foo" "/foo"
   "/../foo" "/foo" ;;was: "/../foo"
   "/foo." "/foo."
   "/.foo" "/.foo"
   "/foo.." "/foo.."
   "/..foo" "/..foo"
   "/./../foo" "/foo" ;;was: "/../foo"
   "/./foo/." "/foo/"
   "/foo/./bar" "/foo/bar"
   "/foo/../bar" "/bar"
   "/foo//" "/foo/"
   "/foo///bar//" "/foo/bar/"})

(comment
        "http://www.foo.com:80/foo"     "http://www.foo.com/foo"
        "http://www.foo.com/foo/../foo"     "http://www.foo.com/foo"
        "http://www.foo.com:8000/foo"   "http://www.foo.com:8000/foo"
        "http://www.foo.com./foo/bar.html" "http://www.foo.com/foo/bar.html"
        "http://www.foo.com.:81/foo"    "http://www.foo.com:81/foo"
        "http://www.foo.com/%7ebar"     "http://www.foo.com/~bar"
        "http://www.foo.com/%7Ebar"     "http://www.foo.com/~bar"
        "ftp://user:pass@ftp.foo.net/foo/bar" 
          "ftp://user:pass@ftp.foo.net/foo/bar"
        "http://USER:pass@www.Example.COM/foo/bar" 
          "http://USER:pass@www.example.com/foo/bar"
        "http://www.example.com./"      "http://www.example.com/"
        "-"                             "-"
        ;; not so sure about this one, the hash mark is questionable
        "http://www.foo.com/?p=529&#038;cpage=1#comment-783" "http://www.foo.com/?p=529&")

(deftest test-normalize
  (let [expected (URI. "http://clojure.org/")
        results (map #(normalize (URI. %))
                     (list "http://clojure.org"
                           "http://clojure.org:80"
                           "http://Clojure.org"))]
    (doseq [result results]
      (is (= expected result)))))

(deftest test-drop-fragment
  (let [uri (URI. "http://clojure.org#foo")
        expected (URI. "http://clojure.org/")]
    (is (= expected (normalize uri :drop-fragment? true)))
    (is (not (= expected (normalize uri))))))

(deftest test-absolute-uris
  (doseq [[s normalize-to-self?] absolute-uris]
    (let [a (URI. s)
          b (normalize a)
          x (.toString a)
          y (.toString b)]
      (if normalize-to-self?
        (is (= x y) (str x " was normalized to " y))
        (is (not (= x y)) (str x " was normalized to " y))))))

(deftest test-relative-uris
  (doseq [[a b] relative-uris]
    (let [expected (URI. b)
          result (normalize (URI. a))]
      (is (= expected result)))))

;; mnot test suite; three tests updated for rfc2396bis.
(def mnot-tests 
[
        "/foo/bar/."                    "/foo/bar/"
        "/foo/bar/./"                   "/foo/bar/"
        "/foo/bar/.."                   "/foo/"
        "/foo/bar/../"                  "/foo/"
        "/foo/bar/../baz"               "/foo/baz"
        "/foo/bar/../.."                "/"
        "/foo/bar/../../"               "/"
        "/foo/bar/../../baz"            "/baz"
        "/foo/bar/../../../baz"         "/baz" ;;was: "/../baz"
        "/foo/bar/../../../../baz"      "/baz"
        "/./foo"                        "/foo"
        "/../foo"                       "/foo" ;;was: "/../foo"
        "/foo."                         "/foo."
        "/.foo"                         "/.foo"
        "/foo.."                        "/foo.."
        "/..foo"                        "/..foo"
        "/./../foo"                     "/foo" ;;was: "/../foo"
        "/./foo/."                      "/foo/"
        "/foo/./bar"                    "/foo/bar"
        "/foo/../bar"                   "/bar"
        "/foo//"                        "/foo/"
        "/foo///bar//"                  "/foo/bar/"
        "http://www.foo.com:80/foo"     "http://www.foo.com/foo"
        "http://www.foo.com/foo/../foo"     "http://www.foo.com/foo"
        "http://www.foo.com:8000/foo"   "http://www.foo.com:8000/foo"
        "http://www.foo.com./foo/bar.html" "http://www.foo.com/foo/bar.html"
        "http://www.foo.com.:81/foo"    "http://www.foo.com:81/foo"
        "http://www.foo.com/%7ebar"     "http://www.foo.com/~bar"
        "http://www.foo.com/%7Ebar"     "http://www.foo.com/~bar"
        "ftp://user:pass@ftp.foo.net/foo/bar" 
          "ftp://user:pass@ftp.foo.net/foo/bar"
        "http://USER:pass@www.Example.COM/foo/bar" 
          "http://USER:pass@www.example.com/foo/bar"
        "http://www.example.com./"      "http://www.example.com/"
        "-"                             "-"
        ;; not so sure about this one, the hash mark is questionable
        "http://www.foo.com/?p=529&#038;cpage=1#comment-783" "http://www.foo.com/?p=529&"
 ])

(deftest test-mnot-tests
  (doall
    (map 
     (fn [[original normalized]]
       (is (= normalized (canonicalize-url original)) 
           (str original " normalized should be " normalized)))
     (partition 2 mnot-tests))))

(comment "these tests don't pass (yet)")
(def failing-tests [
        false "http://example.com/?q=%5c" ;; should be uppercase - todo
        false "http://example.com/?q=%C7"      ;; wrong encoding
        false "http://example.com/?q=C%CC%A7"  ;; wrong encoding

        ;; from rfc2396bis
        true  "ftp://ftp.is.co.za/rfc/rfc1808.txt"
        true  "ldap://[2001:db8::7]/c=GB?objectClass?one"
        true  "mailto:John.Doe@example.com"
        true  "news:comp.infosystems.www.servers.unix"
        true  "tel:+1-816-555-1212"
        true  "urn:oasis:names:specification:docbook:dtd:xml:4.1.2"
        ;; other
        true  "http://www.w3.org/2000/01/rdf-schema#"
  ])


;; (deftest test-failing-tests
;;   (doall
;;     (map 
;;      (fn [[expected url]]
;;        (is (= (= url (canonicalize-url url)) expected)
;;            (str url " normalized incorrectly")))
;;      (partition 2 failing-tests))))

