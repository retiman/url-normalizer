(defproject url-normalizer "0.1.0"
  :description "Normalizes and standardizes URLs in a consistent manner."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.apache.httpcomponents/httpclient "4.1-beta1" :exclusions
                   [commons-logging
                    commons-codec]]]
  :dev-dependencies
    [[swank-clojure "1.2.1"]])
