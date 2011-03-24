(defproject url-normalizer "0.3.4-1"
  :description "Normalizes and standardizes URLs in a consistent manner."
  :min-lein-version "1.4.2"
  :warn-on-reflection true
  :java-source-path "src"
  :dependencies
    [[org.clojure/clojure "1.2.0"]
     [org.clojure/clojure-contrib "1.2.0"]
     [org.apache.abdera/abdera-i18n "1.1.1" :exclusions
       [commons-codec
        geronimo-activation]]
     [org.apache.httpcomponents/httpclient "4.1" :exclusions
       [commons-logging
        commons-codec]]]
  :dev-dependencies
    [[lein-javac "1.2.1-SNAPSHOT"]
     [autodoc "0.7.1"]
     [robert/hooke "1.1.0"]
     [swank-clojure "1.2.1"]]
  :test-selectors
    {:default (fn [t] (not (:failing t)))
     :failing :failing
     :all (fn [_] true)})
