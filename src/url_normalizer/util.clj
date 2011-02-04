(ns url-normalizer.util
  "Utilities and specific normalizations."
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
  ^{:doc
    "A mapping of encoded unreserved characters to their decoded
    counterparts"}
  unreserved
  (assoc
    (merge alpha digits)
    "%2D" "-"
    "%2E" "."
    "%5F" "_"
    "%7E" "~"))

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

(defn lower-case-scheme
  "A safe normalization that lower cases the scheme:

  HTTP://example.com -> http://example.com"
  [scheme ctx]
  (if (:lower-case-scheme? ctx)
    (su/lower-case scheme)
    scheme))

(defn force-http
  "An unsafe normalization that forces the HTTP scheme if HTTPS is encountered:

  https://example.com -> http://example.com"
  [scheme ctx]
  (if (and (:force-http? ctx) (= scheme "https"))
    "http"
    scheme))

(defn remove-www
  "An unsafe normalization that removes the www from a domain:

  http://www.example.com/ -> http://example.com/
  http://www.foo.bar.example.com/ -> http://www.foo.bar.example.com/
  http://www2.example.com/ -> http://www2.example.com/"
  [host ctx]
  (if (:remove-www? ctx)
    (throw (UnsupportedOperationException.))
    host))

(defn lower-case-host
  "A safe normalization that lower cases the host name:

  http://ExAmpLe.com -> http://example.com"
  [host ctx]
  (if (:lower-case-host? ctx)
    (su/lower-case host)
    host))

(defn remove-ip
  "An unsafe normalization that removes the IP:

  http://192.0.32.10 -> http://example.com"
  [host ctx]
  (if (:remove-ip? ctx)
    (throw (UnsupportedOperationException.))
    host))

(defn remove-empty-user-info
  "An unsafe normalization that removes the user info part of a URI:

  http://@example.com -> http://example.com
  http://:@example.com -> http://example.com"
  [user-info ctx]
  (if (and (:remove-empty-user-info? ctx)
           (or (= ":" user-info) (= "" user-info)))
    nil
    user-info))

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
    port))

; TODO: Roll me into normalize-percent-encoding
(defn decode-special-characters
  [text ctx]
  "An unsafe normalization that decodes special characters"
  (if (:decode-special-characters? ctx)
    (throw (UnsupportedOperationException.))
    text))

(defn normalize-percent-encoding
  "Applies several percent encoding normalizations.

  upper-case-percent-encoding:
  http://example.com/%7ejane -> http://example.com/%7Ejane

  decode-unreserved-characters:
  http://example.com/%7ejane -> http://example.com~/jane"
  [text ctx]
  (if (:upper-case-percent-encoding? ctx)
    (loop [sb (StringBuilder.)
           m (re-matcher #"%[a-fA-F0-9]{2}" text)
           k 0]
      (if (nil? (re-find m))
        (do
          (.append sb (.substring text k))
          (.toString sb))
        (let [g (-> m (.group 0) (.toUpperCase))]
          (.append sb (.substring text k (.start m)))
          (if (and (:decode-unreserved-characters? ctx)
                   (contains? unreserved g))
            (.append sb (get unreserved g))
            (.append sb g))
          (recur sb m (.end m)))))
    text))

(defn remove-duplicate-slashes
  "An unsafe normalization that removes duplicate slashes in a path:

  http://example.com/foo//bar/ -> http://example.com/foo/bar"
  [path ctx]
  (if (:remove-duplicate-slashes? ctx)
    (throw (UnsupportedOperationException.))
    path))

(defn add-trailing-slash
  "A safe normalization that adds a slash to an empty path:

  http://example.com -> http://example.com/
  http://example.com/foo -> http://example.com/foo"
  [path ctx]
  (if (and (:add-trailing-slash? ctx) (= "" path)) "/" path))

(defn remove-duplicate-query-keys
  "An unsafe normalization that removes duplicate query keys and values:

  http://example.com/?foo&foo=bar -> http://example.com/?foo=bar"
  [query ctx]
  (if (:remove-duplicate-query-keys? ctx)
    (throw (UnsupportedOperationException.))
    query))

(defn sort-query-keys
  "An unsafe normalization that sorts the query keys and values:

  http://example.com/?c&a&b -> http://example.com/a&b&c"
  [query ctx]
  (if (:sort-query-keys? ctx)
    (throw (UnsupportedOperationException.))
    query))

(defn remove-empty-query
  "An unsafe normalization that removes an empty query:

  http://example.com/? -> http://example.com/
  http://example.com? -> http://example.com"
  [query ctx]
  (if (and (:remove-empty-query? ctx) (= query ""))
    nil
    query))

(defn remove-fragment
  "An unsafe normalization that removes the fragment.  The URI will still refer
  to the same resource so sometimes the fragment is not needed:

  http://example.com/#foo -> http://example.com/"
  [fragment ctx]
  (if (:remove-fragment? ctx)
    nil
    fragment))
