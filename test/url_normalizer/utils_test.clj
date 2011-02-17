(ns url-normalizer.utils-test
  (:use
    [url-normalizer.utils]
    [url-normalizer.test]
    [clojure.test])
  (:gen-class))

(deftest test-remove-directory-index
  (let [ctx {:remove-directory-index? true}]
    (is (= (remove-directory-index "/index.php" ctx) "/"))))
