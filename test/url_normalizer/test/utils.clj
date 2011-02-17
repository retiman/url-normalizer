(ns url-normalizer.test.utils
  (:use
    [url-normalizer.utils]
    [clojure.test])
  (:gen-class))

(defn refer-private
  "Access private symbols of a namespace."
  [ns]
  (doseq [[symbol var] (ns-interns ns)]
    (when (:private (meta var))
      (intern *ns* symbol var))))

(deftest test-remove-directory-index
  (let [ctx {:remove-directory-index? true}]
    (is (= (remove-directory-index "/index.php" ctx) "/"))))
