(ns sigma-chat.test
  (:require [caesium.crypto.box :as b]
            [caesium.crypto.secretbox :as sb]
            [caesium.crypto.generichash :as h]
            [caesium.crypto.scalarmult :as sm]
            [caesium.crypto.sign :as sign])
  (:import [java.nio ByteBuffer]
           [java.lang Byte String]))

(def certificate-auth {:alice "" :bob ""})

(defn byte-arr->hex-string [b-arr]
  (doseq [e b-arr]
    (println (clojure.string/join (map #(.toUpperCase (format "%02x" %)) e)))))

(defn byte-buf->hex-string [b-arr]
  (doseq [e b-arr]
    (println (clojure.string/join (map #(.toUpperCase (format "%02x" %)) (.array e))))))

(def kp1 (b/keypair!))
(def kp2 (b/keypair!))
(def sk1 (:secret kp1))
(def sk2 (:secret kp2))
(def pk1 (:public kp1))
(def pk2 (:public kp2))

#_(byte-buf->hex-string [sk1 sk2 pk1 pk2])

;; Bob
(def sm1 (sm/scalarmult sk1))
;; Alice
(def sm2 (sm/scalarmult sk2))

(println "These are exchanged scalar mults")
(byte-arr->hex-string [sm1 sm2])

(println "This is after the exchange")
(def nmg (sm/scalarmult sk1 pk2))
(def mng (sm/scalarmult sk2 pk1))

(byte-arr->hex-string [nmg mng])

(def public-key-map {:bob pk2 :alice pk1})

(def bob-keys (sign/keyp))

(def bob-sign (sign/sign public-key-map )