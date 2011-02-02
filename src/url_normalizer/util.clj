(ns url-normalizer.util
  (:require
    [clojure.contrib.str-utils2 :as su]))

(defn >>>
  "Takes a set of functions and returns a fn that is the composition
  of those fns.  The returned fn takes a variable number of args,
  applies the rightmost of fns to the args, the next
  fn (left-to-right) to the result, etc."
  ([f] f)
  ([f g]
     (fn
       ([] (g (f)))
       ([x] (g (f x)))
       ([x y] (g (f x y)))
       ([x y z] (g (f x y z)))
       ([x y z & args] (g (apply f x y z args)))))
  ([f g h]
     (fn
       ([] (h (g (f))))
       ([x] (h (g (f x))))
       ([x y] (h (g (f x y))))
       ([x y z] (h (g (f x y z))))
       ([x y z & args] (h (g (apply f x y z args))))))
  ([f1 f2 f3 & fs]
    (let [fs (list* f1 f2 f3 fs)]
      (fn [& args]
        (loop [ret (apply (first fs) args) fs (next fs)]
          (if fs
            (recur ((first fs) ret) (next fs))
            ret))))))

(defn byte-to-hex-string
  "Converts the lower 16 bits of b to into a hex string"
  [b]
  (let [s (Integer/toHexString (bit-and 0xff b))]
    (if (= (count s) 1) (str "0" s) s)))

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

(defn lower-case-host [host ctx]
  (if (:lower-case-host? ctx)
    (su/lower-case host)
    host))

(defn remove-trailing-dot-in-host [host ctx]
  (if (and (:remove-trailing-dot-in-host? ctx)
           (= \. (last host)))
    (apply str (butlast host))
    host))

; TODO: Add other default ports
(defn remove-default-port [port ctx]
  (if (and (:remove-default-port? ctx) (= port 80))
    nil
    (str ":" port)))

(defn get-path [uri ctx]
  (if (:remove-dot-segments? ctx)
    (-> uri (.normalize) (.getRawPath))
    (.getRawPath uri)))

(defn decode-unreserved-characters [path ctx]
  (if (:decode-unreserved-characters? ctx) (decode path) path))

(defn add-trailing-slash [path ctx]
  (if (and (:add-trailing-slash? ctx) (= "" path)) "/" path))
