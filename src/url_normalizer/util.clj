(ns url-normalizer.util
  (:require
    [clojure.contrib.str-utils2 :as su]))

(defn- byte-to-hex-string
  "Converts the lower 16 bits of b to into a hex string."
  [b]
  (let [s (Integer/toHexString (bit-and 0xff b))]
    (if (= (count s) 1) (str "0" s) s)))

(def
  ^{:doc "The default ports for various schemes."}
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

(defn lower-case-host
  "A safe normalization that lower cases the host name:

  http://ExAmpLe.com -> http://example.com"
  [host ctx]
  (if (:lower-case-host? ctx)
    (su/lower-case host)
    host))

(defn remove-empty-user-info
  "An unsafe normalization that removes the user info part of a URI:

  http://@example.com -> http://example.com
  http://:@example.com -> http://example.com"
  [user-info ctx]
  (if (and (:remove-empty-user-info? ctx)
           (or (= ":" user-info) (= "" user-info)))
    nil
    (str user-info "@")))

(defn remove-trailing-dot-in-host
  "An unsafe normalization that removes the trailing dot in a host:

  http://example.com./foo -> http://example.com/foo"
  [host ctx]
  (if (and (:remove-trailing-dot-in-host? ctx)
           (= \. (last host)))
    (apply str (butlast host))
    host))

(defn remove-default-port
  "A safe normalization that removes the the default port:

  http://example.com:80 -> http://example.com
  http://example.com:8080 -> http://example.com/"
  [scheme port ctx]
  (if (and (:remove-default-port? ctx)
           (= port (get default-port scheme)))
    nil
    (str ":" port)))

(defn decode-unreserved-characters
  "A safe normalization that decodes percent encoded characters that don't need
  to be encoded.  According to RFC3986, these characters are:

  unreserved  = ALPHA / DIGIT / - / . / _ / ~

  Here are some example normalizations:

  http://example.com/%7Ejane -> http://example.com/~jane"
  [path ctx]
  (if (:decode-unreserved-characters? ctx)
    ((comp (apply comp decode-alphanum)
           #(.replaceAll % "%2D" "-")
           #(.replaceAll % "%2E" ".")
           #(.replaceAll % "%5F" "_")
           #(.replaceAll % "%7E" "~"))
       path)
    path))

(defn upper-case-percent-encoding
  "A safe normalization that converts percent decodings to uppercase:

  http://example.com/%7ejane -> http://example.com/%7Ejane"
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

(defn add-trailing-slash
  "A safe normalization that adds a slash to an empty path:

  http://example.com -> http://example.com/
  http://example.com/foo -> http://example.com/foo"
  [path ctx]
  (if (and (:add-trailing-slash? ctx) (= "" path)) "/" path))

(defn remove-empty-query
  "An unsafe normalization that removes an empty query:

  http://example.com/? -> http://example.com/
  http://example.com? -> http://example.com"
  [query ctx]
  (if (and (:remove-empty-query? ctx) (= query ""))
    nil
    (str "?" query)))

(defn remove-fragment
  "An unsafe normalization that removes the fragment.  The URI will still refer
  to the same resource so sometimes the fragment is not needed:

  http://example.com/#foo -> http://example.com/"
  [fragment ctx]
  (if (:remove-fragment? ctx)
    nil
    (str "#" fragment)))
