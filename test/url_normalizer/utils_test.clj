(ns url-normalizer.utils-test
  (:use
    [url-normalizer.utils]
    [url-normalizer.test]
    [clojure.test])
  (:gen-class))

(deftest test-remove-directory-index
  (let [ctx {:remove-directory-index? true}]
    (is (= (remove-directory-index "/index.php" ctx) "/"))))

(deftest test-decode-reserved-characters
  (let [ctx {:decode-reserved-characters? true}]
    (is (= (normalize-percent-encoding "/%3Ffoo%3Dbar%26bif%3Dbaz" ctx)
           "/?foo=bar&bif=baz"))))
