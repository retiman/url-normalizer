(ns url-normalizer.core
  (:refer-clojure :exclude (resolve))
  (:use
    [url-normalizer.util])
  (:require
    [clojure.contrib.str-utils2 :as su])
  (:import
    [java.net URL URI]
    [org.apache.http HttpHost]
    [org.apache.http.client.utils URIUtils]))

(defn as-url
  [arg]
  (if (= URL (type arg)) arg (URL. arg)))

(defn as-uri
  [arg]
  (if (= URI (type arg)) arg (URI. arg)))

(defn- nil-host?
  [uri]
  (or (nil? uri) (nil? (.getHost uri))))

(def *safe-normalizations*
  {:lower-case-scheme? true
   :lower-case-host? true
   :upper-case-percent-encoding? true
   :decode-unreserved-characters? true
   :add-trailing-slash? true
   :remove-default-port? true
   :remove-dot-segments? true})

(def *unsafe-normalizations*
  {:remove-directory-index? false
   :remove-fragment? false
   :remove-ip? false
   :remove-duplicate-slash? false
   :force-http? false
   :remove-www? false
   :sort-query? false
   :remove-duplicate-query? false
   :remove-question-mark? false
   :decode-special-characters? false})

(def *context*
  (merge *safe-normalizations* *unsafe-normalizations*))

(defn- create-http-host
  "Create an org.apache.http.HttpHost with the host name in lowercase.
  Also removes the default port for the HTTP scheme."
  [uri]
  (if (nil-host? uri)
    nil
    (let [scheme (.getScheme uri)
          host (su/lower-case (.getHost uri))
          port (.getPort uri)]
      (if (and (= scheme "http") (= port 80))
        (HttpHost. host)
        (HttpHost. host port scheme)))))

(defn- create-uri
  [& {:keys [scheme user-info host port path query fragment]}]
  (let [buffer (StringBuilder.)]
    (if-not (nil? host)
      (do
        (if-not (nil? scheme)
          (.append buffer (str scheme "://")))
        (if-not (or (nil? user-info) (= ":" user-info) (= "" user-info))
          (.append buffer (str user-info "@")))
        (.append buffer host)
        (if (> port 0)
          (.append buffer (str ":" port)))))
    (if (or (nil? path) (not (.startsWith path "/")))
      (.append buffer "/"))
    (if-not (nil? path)
      (.append buffer path))
    (if-not (nil? query)
      (.append buffer (str "?" query)))
    (if-not (nil? fragment)
      (.append buffer (str "#" fragment)))
    (URI. (.toString buffer))))

(defn- decode
  "Decodes percent encoded octets to their corresponding characters.
  Only decodes unreserved characters."
  [path]
  ((comp (apply comp decode-alphanum)
         #(.replaceAll % "%2D" "-")
         #(.replaceAll % "%2E" ".")
         #(.replaceAll % "%5F" "_")
         #(.replaceAll % "%7E" "~"))
     path))

(defn- rewrite
  "Rewrites the URI, possibly dropping the fragment."
  [http-host uri drop-fragment?]
  (URIUtils/rewriteURI uri http-host drop-fragment?))

(defn- resolve
  "Resolve a URI reference against a base URI by removing dot segments."
  [base uri]
  (URIUtils/resolve base uri))

(defn normalize
  [arg]
  (let [uri (as-uri arg)
        http-host (create-http-host uri)
        host (URI. (str (.getScheme uri) "://" (.getHost uri)))
        f (comp #(rewrite http-host % false)
                #(resolve host %))
        result (if http-host (f uri) uri)]
      (create-uri :scheme (.getScheme result)
                  :user-info (.getRawUserInfo uri)
                  :host (.getHost result)
                  :port (.getPort result)
                  :path (decode (.getRawPath result))
                  :query (.getRawQuery result)
                  :fragment (.getRawFragment result))))

(defn equivalent?
  "Returns true if the two URIs are equivalent when normalized."
  [a b]
  (= (normalize a) (normalize b)))

(comment
(defn normalize-
  ([uri]
    (normalize- *context*))
  ([uri ctx]
    (create-uri :scheme (normalize-scheme uri ctx)
                :user-info (normalize-user-info uri ctx)
                :host (normalize-host uri ctx)
                :port (normalize-port uri ctx)
                :path (normalize-path uri ctx)
                :query (normalize-query uri ctx)
                :fragment (normalize-fragment uri ctx)))))


(def default-port
{
 "ftp" 21
 "telnet" 23
 "http" 80
 "gopher" 70
 "news" 119
 "nntp" 119
 "prospero" 191
 "https" 443
 "snews" 563
 "snntp" 563
})

(defn normalize-port [uri]
  (let [scheme (.getScheme uri)
        port (.getPort uri)]
    (if (or (nil? port)
            (= port -1)
            (and (contains? default-port scheme)
                 (= port (default-port scheme))))
      nil
      (str ":" port))))

(defn normalize-path-dot-segments [uri]
  (if-let [path (.getPath uri)]
   (let [segments (su/split path #"/" -1)
         ;;x (prn segments)
         ;; resolve relative paths
         segs2 (reduce
                (fn [acc segment]
                  (cond
                   (= "" segment ) (if (> (count acc) 0)
                                     acc
                                     (concat acc [segment]))
                   (= "."  segment) acc
                   (= ".." segment) (if (> (count acc) 1)
                                      (drop-last acc)
                                      acc)
                   true (concat acc [segment])
                   )) [] segments)
         ;; add a slash if the last segment is "" "." ".."
         new-segments (if (contains? #{"" "." ".."} (last segments))
                        (concat segs2 [nil])
                        segs2)]
     (su/join "/" new-segments))))

(defn only-percent-encode-where-essential [path]
  (comment "Where is it non-essential besides tilde ~ ?. a bit of a hack, will extend as new test cases are presented. see: http://labs.apache.org/webarch/uri/rfc/rfc3986.html#unreserved" )
  (su/replace path #"(?i:%7e)" "~"))

(defn normalize-path [uri]
  (let [path (normalize-path-dot-segments uri)
        path2 (only-percent-encode-where-essential path)]
    ;; (if (or (= path "") (= path "/")) "" path)
    path2))

(defn normalize-host [uri]
  (if-let [host (.getHost uri)]
    (let [lhost (su/lower-case host)]
      (if (= (last (seq lhost)) \.)
        (su/join "" (drop-last (seq lhost)))
        lhost))))

(defn- normalize-scheme [uri ctx]
  (if-let [scheme (.getScheme uri)]
    (if (:lower-case-scheme? ctx)
      (su/lower-case scheme))))

(defn normalize-user-info [uri]
  (let [user-info (.getUserInfo uri)]
    (if (and user-info
             (not (contains? #{"" ":"} user-info)))
      (str user-info "@")
      "")))

(defn normalize-query [uri] ;; TODO
  (if-let [q (.getQuery uri)]
    (str "?" q)))

(defmulti to-uri class)
(defmethod to-uri URL [url]
   (URI. (.getProtocol url)
         (.getUserInfo url)
         (.getHost url)
         (.getPort url)
         (.getPath url)
         (.getQuery url)
         (.getRef url)))
;; (defmethod to-uri String [url]
;;  (to-uri (URL. url)))

(defmulti canonicalize-url class)
(defmethod canonicalize-url URI [uri]
 (let [scheme (normalize-scheme uri *context*)
       scheme-connector (if scheme "://" "")
       user-info  (normalize-user-info uri)
       host  (normalize-host uri)
       port  (normalize-port uri)
       path  (normalize-path uri)
       query (normalize-query uri)]
    (str scheme scheme-connector user-info host port path query)))
(defmethod canonicalize-url URL [url] (canonicalize-url (to-uri url)))
(defmethod canonicalize-url String [url]
  (try
    (canonicalize-url (to-uri (URL. url)))
    (catch java.net.URISyntaxException    e (canonicalize-url (URI. url)))
    (catch java.net.MalformedURLException e (canonicalize-url (URI. url)))
    ))

(defmulti url-equal? (fn [a b] [(class a) (class b)]))

(defmethod url-equal? [String String] [url1 url2]
           (let [u1 (canonicalize-url (URI. url1))
                 u2 (canonicalize-url (URI. url2))]
             (= u1 u2)))
