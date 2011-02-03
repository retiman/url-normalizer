(ns url-normalizer.core
  (:refer-clojure :exclude (resolve))
  (:use
    [url-normalizer.util])
  (:require
    [clojure.contrib.str-utils2 :as su])
  (:import
    [java.net URL URI URISyntaxException MalformedURLException]
    [org.apache.http HttpHost]
    [org.apache.http.client.utils URIUtils]))

(defmulti as-url class)
(defmethod as-url String [arg] (URL. arg))
(defmethod as-url URI [arg] (.toURL arg))
(defmethod as-url URL [arg] arg)

(defmulti as-uri class)
(defmethod as-uri String [arg] (URI. arg))
(defmethod as-uri URI [arg] arg)
(defmethod as-uri URL [arg]
  (URI. (.getProtocol arg)
        (.getUserInfo arg)
        (.getHost arg)
        (.getPort arg)
        (.getPath arg)
        (.getQuery arg)
        (.getRef arg)))

(def default-port
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

(def *safe-normalizations*
  {:lower-case-scheme? true
   :lower-case-host? true
   :upper-case-percent-encoding? true
   :decode-unreserved-characters? true
   :encode-illegal-characters? true
   :add-trailing-slash? true
   :remove-default-port? true
   :remove-dot-segments? true})

(def *unsafe-normalizations*
  {:remove-directory-index? false
   :remove-fragment? false
   :remove-ip? false
   :remove-duplicate-slash? false
   :remove-duplicate-query? false
   :remove-empty-query? false
   :remove-empty-user-info? false
   :remove-trailing-dot-in-host? false
   :force-http? false
   :remove-www? false
   :sort-query? false
   :decode-special-characters? false})

(def *context*
  (merge *safe-normalizations* *unsafe-normalizations*))

(defn- resolve
  "Resolve a URI reference against a base URI by removing dot segments."
  [base uri]
  (URIUtils/resolve base uri))

(defn- normalize-scheme-part [uri ctx]
  (if-let [scheme (.getScheme uri)]
    (if (:lower-case-scheme? ctx)
      (str (su/lower-case scheme) "://")
      (str scheme "://"))))

(defn- normalize-user-info-part [uri ctx]
  (if-let [user-info (get-user-info uri ctx)]
    (remove-empty-user-info user-info ctx)))

(defn- normalize-host-part [uri ctx]
  (if-let [host (.getHost uri)]
    ((comp #(remove-trailing-dot-in-host % ctx)
           #(lower-case-host % ctx))
       host)))

(defn normalize-port-part [uri ctx]
  (let [scheme (.getScheme uri)
        port (.getPort uri)]
    (if (and port (not= -1 port))
      (remove-default-port port ctx))))

(defn normalize-path-part [uri ctx]
  (if-let [path (get-path uri ctx)]
    ((comp #(add-trailing-slash % ctx)
           #(decode-unreserved-characters % ctx))
       path)))

(defn- normalize-query-part [uri ctx]
  (if-let [query (get-query uri ctx)]
    (remove-empty-query query ctx)))

(defn- normalize-fragment-part [uri ctx]
  (if-let [fragment (get-fragment uri ctx)]
    (remove-fragment fragment ctx)))

(defn normalize
  ([arg]
    (normalize arg *context*))
  ([arg context]
    (let [uri (as-uri arg)
          ctx (merge *context* context)
          scheme (normalize-scheme-part uri ctx)
          user-info (normalize-user-info-part uri ctx)
          host (normalize-host-part uri ctx)
          port (normalize-port-part uri ctx)
          path (normalize-path-part uri ctx)
          query (normalize-query-part uri ctx)
          fragment (normalize-fragment-part uri ctx)]
      (URI. (str scheme user-info host port path query fragment)))))

(defn equivalent?
  "Returns true if the two URIs are equivalent when normalized."
  [a b]
  (= (normalize a) (normalize b)))

(defn to-uri
  "DEPRECATED: Prefer as-uri."
  {:deprecated "0.1.0"}
  [arg]
  (as-uri arg))

(defn to-url
  "DEPRECATED: Prefer as-url."
  {:deprecated "0.1.0"}
  [arg]
  (as-url arg))

(defn canonicalize-url
  "DEPRECATED: Prefer normalize."
  {:deprecated "0.1.0"}
  [arg]
  (try
    (normalize arg)
    (catch URISyntaxException e (canonicalize-url (to-uri arg)))
    (catch MalformedURLException e (canonicalize-url (to-url arg)))))

(defn url-equal?
  "DEPRECATED: Prefer equivalent?"
  {:deprecated "0.1.0"}
  [a b]
  (equivalent? a b))
