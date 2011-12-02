{:namespaces
 ({:source-url nil,
   :wiki-url "url-normalizer.core-api.html",
   :name "url-normalizer.core",
   :doc
   "A Clojure library for normalizing urls with configurable aggressiveness."}
  {:source-url nil,
   :wiki-url "url-normalizer.utils-api.html",
   :name "url-normalizer.utils",
   :doc "Utilities and specific normalizations."}),
 :vars
 ({:file "src/url_normalizer/core.clj",
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/*context*",
   :namespace "url-normalizer.core",
   :line 68,
   :var-type "var",
   :doc
   "A normalization context. See #'url-normalizer/*safe-normalizations* and\n#'url-normalizer/*unsafe-normalizations* for possible normalizations.",
   :name "*context*"}
  {:arglists ([arg]),
   :name "canonicalize-url",
   :namespace "url-normalizer.core",
   :source-url nil,
   :deprecated "0.1.0",
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/canonicalize-url",
   :doc "DEPRECATED: Prefer normalize.",
   :var-type "function",
   :line 207,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([a b]),
   :name "equal?",
   :namespace "url-normalizer.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/equal?",
   :doc
   "Returns true if the ASCII string versions of URIs are equal.  This is\ndifferent from #'url-normalizer.core/equivalent? as two equivalent URIs\nmay not have the same ASCII string representation.\n\nFor example, the following URIs are equivalent but not equal:\n\n  http://example.com/%7b\n  http://example.com/%7B",
   :var-type "function",
   :line 177,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([a b] [a b context]),
   :name "equivalent?",
   :namespace "url-normalizer.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/equivalent?",
   :doc
   "Returns true if the two URIs are equivalent when normalized.\n\nFor example, the following two URIs are equivalent but not equal:\n\n  http://example.com/%7b\n  http://example.com/%7B",
   :var-type "function",
   :line 165,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([arg] [arg context]),
   :name "normalize",
   :namespace "url-normalizer.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/normalize",
   :doc
   "By default normalizes a URI using safe normalizations.  The URI is expected\nto be a URL with either the HTTP or HTTPS scheme.\n\nYou may specify a normalization context in order to apply non-semantic\npreserving normalizations.",
   :var-type "function",
   :line 139,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([base uri]),
   :name "resolve",
   :namespace "url-normalizer.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/resolve",
   :doc
   "Resolve a URI reference against a base URI by removing dot segments.  The\nApache HttpClient version is used instead of the resolve method on URI due\nto a bug in the Java standard library.\n\nSee <http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>",
   :var-type "function",
   :line 129,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([arg]),
   :name "to-uri",
   :namespace "url-normalizer.core",
   :source-url nil,
   :deprecated "0.1.0",
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/to-uri",
   :doc "DEPRECATED: Prefer as-uri.",
   :var-type "function",
   :line 195,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([arg]),
   :name "to-url",
   :namespace "url-normalizer.core",
   :source-url nil,
   :deprecated "0.1.0",
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/to-url",
   :doc "DEPRECATED: Prefer as-url.",
   :var-type "function",
   :line 201,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([a b]),
   :name "url-equal?",
   :namespace "url-normalizer.core",
   :source-url nil,
   :deprecated "0.1.0",
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/url-equal?",
   :doc "DEPRECATED: Prefer equivalent?",
   :var-type "function",
   :line 219,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([context f]),
   :name "with-normalization-context",
   :namespace "url-normalizer.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.core-api.html#url-normalizer.core/with-normalization-context",
   :doc "Evaluates a function with *context* bound to context",
   :var-type "function",
   :line 189,
   :file "src/url_normalizer/core.clj"}
  {:arglists ([path ctx]),
   :name "add-trailing-slash",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/add-trailing-slash",
   :doc
   "A safe normalization that adds a slash to an empty path:\n\nhttp://example.com -> http://example.com/\nhttp://example.com/foo -> http://example.com/foo",
   :var-type "function",
   :line 205,
   :file "src/url_normalizer/utils.clj"}
  {:file "src/url_normalizer/utils.clj",
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/alpha",
   :namespace "url-normalizer.utils",
   :line 28,
   :var-type "var",
   :doc "Maps percent encoded octets to alpha characters.",
   :name "alpha"}
  {:file "src/url_normalizer/utils.clj",
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/decode-alphanum",
   :namespace "url-normalizer.utils",
   :line 62,
   :var-type "var",
   :doc "A list of functions that decode alphanumerics in a String.",
   :name "decode-alphanum"}
  {:file "src/url_normalizer/utils.clj",
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/default-port",
   :namespace "url-normalizer.utils",
   :line 14,
   :var-type "var",
   :doc "The default ports for various schemes.",
   :name "default-port"}
  {:file "src/url_normalizer/utils.clj",
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/digits",
   :namespace "url-normalizer.utils",
   :line 35,
   :var-type "var",
   :doc "Maps percent encoded octets to digits.",
   :name "digits"}
  {:arglists ([scheme ctx]),
   :name "force-http",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/force-http",
   :doc
   "An unsafe normalization that forces the HTTP scheme if HTTPS is encountered:\n\nhttps://example.com -> http://example.com",
   :var-type "function",
   :line 92,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([host ctx]),
   :name "lower-case-host",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/lower-case-host",
   :doc
   "A safe normalization that lower cases the host name:\n\nhttp://ExAmpLe.com -> http://example.com",
   :var-type "function",
   :line 112,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([scheme ctx]),
   :name "lower-case-scheme",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/lower-case-scheme",
   :doc
   "A safe normalization that lower cases the scheme:\n\nHTTP://example.com -> http://example.com",
   :var-type "function",
   :line 83,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([text ctx]),
   :name "normalize-percent-encoding",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/normalize-percent-encoding",
   :doc
   "Applies several percent encoding normalizations.\n\nupper-case-percent-encoding:\nhttp://example.com/%7ejane -> http://example.com/%7Ejane\n\ndecode-unreserved-characters:\nhttp://example.com/%7ejane -> http://example.com~/jane\n\ndecode-reserved-characters:\nhttp://example.com//%3Ffoo%3Dbar%26bif%3Dbaz -> http://example.com/?foo=bar&bif=baz",
   :var-type "function",
   :line 160,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([scheme port ctx]),
   :name "remove-default-port",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-default-port",
   :doc
   "A safe normalization that removes the the default port:\n\nhttp://example.com:80 -> http://example.com\nhttp://example.com:8080 -> http://example.com/",
   :var-type "function",
   :line 150,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([path ctx]),
   :name "remove-directory-index",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-directory-index",
   :doc
   "An unsafe normalization that removes the directory index from the path.\n\nhttp://www.example.com/index.php -> http://www.example.com/",
   :var-type "function",
   :line 195,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([query ctx]),
   :name "remove-duplicate-query-keys",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-duplicate-query-keys",
   :doc
   "An unsafe normalization that removes duplicate query keys and values:\n\nhttp://example.com/?foo&foo=bar -> http://example.com/?foo=bar",
   :var-type "function",
   :line 213,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([query ctx]),
   :name "remove-empty-query",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-empty-query",
   :doc
   "An unsafe normalization that removes an empty query:\n\nhttp://example.com/? -> http://example.com/\nhttp://example.com? -> http://example.com",
   :var-type "function",
   :line 236,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([user-info ctx]),
   :name "remove-empty-user-info",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-empty-user-info",
   :doc
   "An unsafe normalization that removes the user info part of a URI:\n\nhttp://@example.com -> http://example.com\nhttp://:@example.com -> http://example.com",
   :var-type "function",
   :line 130,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([fragment ctx]),
   :name "remove-fragment",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-fragment",
   :doc
   "An unsafe normalization that removes the fragment.  The URI will still refer\nto the same resource so sometimes the fragment is not needed:\n\nremove-fragment:\nhttp://example.com/#foo -> http://example.com/\n\nremove-fragment and keep-hashbang-fragment:\nhttp://twitter.com/#foo -> http://twitter.com/#foo\nhttp://twitter.com/#!/user -> http://twitter.com/#!/user\n\nSee <http://code.google.com/web/ajaxcrawling/docs/getting-started.html>\nSee <http://www.tbray.org/ongoing/When/201x/2011/02/09/Hash-Blecch>",
   :var-type "function",
   :line 245,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([host ctx]),
   :name "remove-ip",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-ip",
   :doc
   "An unsafe normalization that removes the IP:\n\nhttp://192.0.32.10 -> http://example.com",
   :var-type "function",
   :line 121,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([host ctx]),
   :name "remove-trailing-dot-in-host",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-trailing-dot-in-host",
   :doc
   "An unsafe normalization that removes the trailing dot in a host:\n\nhttp://example.com./foo -> http://example.com/foo",
   :var-type "function",
   :line 140,
   :file "src/url_normalizer/utils.clj"}
  {:arglists ([host ctx]),
   :name "remove-www",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/remove-www",
   :doc
   "An unsafe normalization that removes the www from a domain:\n\nhttp://www.example.com/ -> http://example.com/\nhttp://www.foo.bar.example.com/ -> http://www.foo.bar.example.com/\nhttp://www2.example.com/ -> http://www2.example.com/",
   :var-type "function",
   :line 101,
   :file "src/url_normalizer/utils.clj"}
  {:file "src/url_normalizer/utils.clj",
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/reserved",
   :namespace "url-normalizer.utils",
   :line 54,
   :var-type "var",
   :doc
   "A mapping of encoded reserved characters to their decoded\ncounterparts.",
   :name "reserved"}
  {:arglists ([query ctx]),
   :name "sort-query-keys",
   :namespace "url-normalizer.utils",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/sort-query-keys",
   :doc
   "An unsafe normalization that sorts the query keys and values:\n\nhttp://example.com/?c&a&b -> http://example.com/a&b&c",
   :var-type "function",
   :line 224,
   :file "src/url_normalizer/utils.clj"}
  {:file "src/url_normalizer/utils.clj",
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "/url-normalizer.utils-api.html#url-normalizer.utils/unreserved",
   :namespace "url-normalizer.utils",
   :line 42,
   :var-type "var",
   :doc
   "A mapping of encoded unreserved characters to their decoded\ncounterparts.",
   :name "unreserved"})}
