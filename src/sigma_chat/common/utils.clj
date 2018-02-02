(ns sigma-chat.common.utils
  (:import [java.util UUID]
           [java.util Base64]
           [java.util Date]
           [java.nio ByteBuffer]
           [java.lang Byte String])
  (:require [clojure.string :as str]
            [buddy.hashers :as hash]
            [buddy.core.nonce :as nonce]
            [clojure.tools.logging :as log]
            [clj-time.format :as time-f]
            [clj-time.coerce :as time-c]))

; https://funcool.github.io/buddy-hashers/latest/#algorithm-tunning-params
(def pbkdf-alg {:alg :scrypt
                :cpucost 65536
                :memcost 10
                :parallelism 1})

(defn pbkdf [pw]
  (hash/derive pw pbkdf-alg))

(defn pbkdf= [pw verifier]
  (hash/check pw verifier {:limit #{(:alg pbkdf-alg)}}))

(defn random-bytes [n]
  (nonce/random-bytes n))

(defn uuid
  ([] (UUID/randomUUID))
  ([s]
   (if (uuid? s)
     s
     (UUID/fromString s))))

(def blank? str/blank?)

(def not-blank? (complement blank?))

(def date-formatter (time-f/formatter "yyyy-MM-dd"))

(defn str->epoch [date]
  (time-c/to-long (time-f/parse date-formatter date)))

(defn epoch->str [date]
  (time-f/unparse date-formatter (time-c/from-long date)))

(defn valid-date? [date]
  #(try
     (epoch->str (str->epoch date))
     (catch Exception _)))

(defn bytes->base64-url-string [b]
  (.encodeToString (Base64/getUrlEncoder) b))

(defn bytes->base64-string [b]
  (.encodeToString (Base64/getEncoder) b))

(defn parse-int [x]
  (if x (Integer/parseInt x) nil))

(defn byte-arr->hex-string [b-arr]
  (clojure.string/join (map #(.toUpperCase (format "%02x" %)) b-arr)))

(defn byte-buf->hex-string [b-arr]
  (clojure.string/join (map #(.toUpperCase (format "%02x" %)) (.array b-arr))))
