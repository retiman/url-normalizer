(defproject url-normalizer "0.2.0"
  :description "Normalizes and standardizes URLs in a consistent manner."
  :min-lein-version "1.4.2"
  :warn-on-reflection true
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.apache.httpcomponents/httpclient "4.1" :exclusions
                   [commons-logging
                    commons-codec]]]
  :dev-dependencies
    [[autodoc "0.7.1"]
     [swank-clojure "1.2.1"]])
