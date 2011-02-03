(ns url-normalizer.util
  (:require
    [clojure.contrib.str-utils2 :as su]))

(defn- byte-to-hex-string
  "Converts the lower 16 bits of b to into a hex string"
  [b]
  (let [s (Integer/toHexString (bit-and 0xff b))]
    (if (= (count s) 1) (str "0" s) s)))

(def
  ^{:doc "The default ports for various schemes"}
  default-port
  {"ftp" 21
   "telnet" 23
   "http" 80
   "gopher" 70
   "news" 119
   "nntp" 119
   "prospero" 191
   "https" 443
   "snews" 563
   "snntp" 563})

(def
  ^{:doc "Maps percent encoded octets to alpha characters."}
  alpha
  (let [xs (concat (range 0x41 (inc 0x5A)) (range 0x61 (inc 0x7A)))]
    (zipmap (map #(str "%" (su/upper-case (byte-to-hex-string %))) xs)
            (map #(str (char %)) xs))))

(def
  ^{:doc "Maps percent encoded octets to digits."}
  digits
  (let [xs (range 0x30 (inc 0x39))]
    (zipmap (map #(str "%" (byte-to-hex-string %)) xs)
            (map #(str (char %)) xs))))

(def
  ^{:doc "A list of functions that decode alphanumerics in a String."}
  decode-alphanum
  (concat
    (map #(fn [s] (.replaceAll s (first %) (last %)))
         (concat alpha digits))))

(defn get-user-info [uri ctx]
  (if (:encode-illegal-characters? ctx)
    (.getRawUserInfo uri)
    (.getUserInfo uri)))

(defn get-path [uri ctx]
  (if (:remove-dot-segments? ctx)
    (-> uri (.normalize) (.getRawPath))
    (.getRawPath uri)))

(defn get-query [uri ctx]
  (if (:encode-illegal-characters? ctx)
    (.getRawQuery uri)
    (.getQuery uri)))

(defn get-fragment [uri ctx]
  (if (:encode-illegal-characters? ctx)
    (.getRawFragment uri)
    (.getFragment uri)))

(defn lower-case-host [host ctx]
  (if (:lower-case-host? ctx)
    (su/lower-case host)
    host))

(defn remove-empty-user-info [user-info ctx]
  (if (and (:remove-empty-user-info? ctx)
           (or (= ":" user-info) (= "" user-info)))
    nil
    (str user-info "@")))

(defn remove-trailing-dot-in-host [host ctx]
  (if (and (:remove-trailing-dot-in-host? ctx)
           (= \. (last host)))
    (apply str (butlast host))
    host))

(defn remove-default-port [scheme port ctx]
  (if (and (:remove-default-port? ctx)
           (= port (get default-port scheme)))
    nil
    (str ":" port)))

(defn decode-unreserved-characters [path ctx]
  (if (:decode-unreserved-characters? ctx)
    ((comp (apply comp decode-alphanum)
           #(.replaceAll % "%2D" "-")
           #(.replaceAll % "%2E" ".")
           #(.replaceAll % "%5F" "_")
           #(.replaceAll % "%7E" "~"))
       path)
    path))

(defn upper-case-percent-encoding
  "Why is this so hard in Java?

  See <http://stackoverflow.com/questions/2770967/use-java-and-regex-to-convert-casing-in-a-string>"
  [text ctx]
  (if (:upper-case-percent-encoding? ctx)
    (loop [sb (StringBuilder.)
           m (re-matcher #"%[a-f0-9]{2}" text)
           k 0]
      (let [t (re-find m)]
        (if (nil? t)
          (do
            (.append sb (.substring text k))
            (.toString sb))
          (do
            (.append sb (.substring text k (.start m)))
            (.append sb (-> m (.group 0) (.toUpperCase)))
            (recur sb m (.end m))))))
    text))

(defn add-trailing-slash [path ctx]
  (if (and (:add-trailing-slash? ctx) (= "" path)) "/" path))

(defn remove-empty-query [query ctx]
  (if (and (:remove-empty-query? ctx) (= query ""))
    nil
    (str "?" query)))

(defn remove-fragment [fragment ctx]
  (if (:remove-fragment? ctx)
    nil
    (str "#" fragment)))
