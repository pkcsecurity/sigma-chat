(ns sigma-chat.common.spec
  (:require [clojure.spec.alpha :as s]
            [sigma-chat.common.response :as response]
            [sigma-chat.common.utils :as utils]
            [sigma-chat.common.properties :as properties]))

(defn explain [spec obj]
  (when-let [probs (s/explain-data spec obj)]
    (with-out-str
      (s/explain-out probs))))

(defmacro string-max-n [n]
  `(s/and string? utils/not-blank? #(< (count %) ~n)))

(defmacro string-of-length [n]
  `(s/and string? utils/not-blank? #(= (count %) ~n)))

(def string (string-max-n 256))

(def long-string (string-max-n 1024))

(def strong-password (s/and string #(>= (count %) 8)))

(def numeric
  (s/and
    string?
    utils/not-blank?
    (partial re-matches #"[0-9]+")))

(def uuid
  (s/and
    (string-of-length 36)
    #(try
       (utils/uuid %)
       (catch Exception _))))

(defn conform [kw obj]
  (let [parsed (s/conform kw obj)]
    (if (= parsed ::s/invalid)
      (throw (ex-info (explain kw obj) {::error kw}))
      (s/unform kw parsed))))

; NOTE:
; When we are in prod, we don't want to leak info about our
; validators.
(defn wrap-conform-failure [handler]
  (fn [req]
    (try
      (handler req)
      (catch clojure.lang.ExceptionInfo ei
        (if-let [kw (::error (ex-data ei))]
          (response/bad-request (if (= (properties/get-prop :common :mode) "PROD") (str "error: " kw) (.getMessage ei)))
          (if (= (properties/get-prop :common :mode) "PROD")
            response/internal-server-error
            (throw ei)))))))

(def str-int
  #(try
     (Long/parseLong %)
     (catch Exception _)))

(def str-boolean
  #(try
     (Boolean/parseBoolean %)
     (catch Exception _)))

