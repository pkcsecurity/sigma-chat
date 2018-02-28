(ns sigma-chat.server.sigma
  (:require [clojure.tools.logging :as log]
            [sigma-chat.common.utils :as utils]
            [clj-http.util :refer [opt] :as http]
            [caesium.crypto.box :as box]
            [caesium.crypto.sign :as sign]
            [caesium.byte-bufs :as bb]
            [caesium.crypto.scalarmult :as sm]
            [cheshire.core :refer [parse-string generate-string]]
            [buddy.core.codecs :as codecs]
            [buddy.core.kdf :as kdf]
            [buddy.core.mac :as mac]
            [buddy.core.nonce :as nonce]
            [sigma-chat.common.properties :as props]))

(def vault (atom {}))

(defn get-key [id]
  (when-not (:kdf @vault)
    (swap! vault assoc :kdf
      {:counter 0
       :engine (kdf/engine {:alg :hkdf+sha256
                            :key (get-in @vault [id :shared])})}))
  (swap! vault update-in [:kdf :counter] inc)
  (kdf/get-bytes (get-in @vault [:kdf :engine]) 32))

(defn mac-message [key msg & {:keys [salt] :or {salt (nonce/random-bytes 16)}}]
  (log/info (codecs/bytes->hex key) msg (codecs/bytes->hex salt))
  {:mac (mac/hash msg {:key key :iv salt :alg :poly1305+aes})
   :salt salt})


(defn gen-keys
  "Generates a private, public, and shared key using caesium box and scalar multiplication.

  Keys are stored in vault under the id as secret, public, and shared. Human readable hex values are
  stored as secret-prt, public-prt, shared-prt. If a seed is passed, it is also stored
  in the vault as seed and seed-prt under id for the raw and hex values respectively."
  ([id key]
   (let [keys (box/keypair!)
         shared (sm/scalarmult (get keys :secret) key)
         shared-prt (utils/byte-arr->hex-string shared)
         secret-prt (utils/byte-buf->hex-string (get keys :secret))
         public-prt (utils/byte-buf->hex-string (get keys :public))
         keys (assoc keys :secret-prt secret-prt :public-prt public-prt
                          :shared shared :shared-prt shared-prt)]
     (swap! vault assoc id keys)
     (log/info "SERVER: Generated secret key: " secret-prt)
     (log/info "SERVER: Generated public key: " public-prt)
     (log/info "SERVER: Generated shared key: " shared-prt)))
  ([id key seed]
   (let [keys (box/keypair! seed)
         shared (sm/scalarmult (get keys :secret) key)
         shared-prt (utils/byte-buf->hex-string shared)
         secret-prt (utils/byte-buf->hex-string (get keys :secret))
         public-prt (utils/byte-buf->hex-string (get keys :public))
         seed (bb/->indirect-byte-buf seed)
         seed-prt (utils/byte-buf->hex-string seed)
         keys (assoc keys :secret-prt secret-prt :public-prt public-prt
                          :shared shared :shared-prt shared-prt
                          :seed seed :seed-prt seed-prt)]
     (swap! vault assoc id keys)
     (log/info "SERVER: Generated secret key with seed [" seed-prt "]: " secret-prt)
     (log/info "SERVER: Generated public key with seed [" seed-prt "]: " public-prt)
     (log/info "SERVER: Generated shared key with seed [" seed-prt "]: " shared-prt))))

(defn init
  "Initiates a communication with a client based on a key passed in the route-params.

  From the key, the server generates a shared secret using Ellicptic curve diffie-helman.
  The response sent back is a map with the following key-value pairs:
    :message-num - The counter for which message this is. Also in the Key derivation function
                    for key streching. INSERT ALGORITH.
    :key         - Public key of server based in the same Elliptic cure diffie-helman scheme.
    :message     - A signed message using the shared secret generated from performaing scalar
                    multiplication with the given public key and the server's public key - extended
                    by the Key Derivation function with the message-num ans the nonce.

     In the message is a map containing:
      :identity - This is the public key of the server that can be attained from the certificate authority
      :raw-sign - Body of the signature before it is signed.
      :sign     - Signature containing a map of the clients public key and the server's public key.
      :MAC      - MAC of the server's public key held by the certificate authority signed with the shared secret"
  [req & seed]
  (let [key (utils/hex-string->byte-buf (get-in req [:body :key]))
        key-prt (utils/byte-buf->hex-string key)
        id key-prt
        ip (get req :remote-addr)]

    (log/info "SERVER: Received initiation sequence from [" ip "] with key [" id "].")

    (if seed
      (gen-keys id key (if (coll? seed) (first seed) seed))
      (gen-keys id key))


    (log/info {:client-key key-prt :server-key (get-in @vault [id :public])})


    (let [cert (props/get-prop :ca :server-public-key)
          cert-hex (utils/byte-buf->hex-string cert)
          raw-sign (generate-string {:client-key key-prt :server-key (get-in @vault [id :public-prt])})
          signature (sign/signed (.getBytes raw-sign) (props/get-prop :ca :server-public-key))
          mac (assoc (mac-message (get-key id) cert-hex) :counter (get-in @vault [:kdf :counter]))]

      "We need to do the KDF using buddy core and the shared-secret https://funcool.github.io/buddy-core/latest/"
      "We need to MAC the server public key with part of the shared key using the KDF -> and the incrementer"
      "Ask becker if the KM and KE should be different per transaction"

      (log/info cert)
      (log/info cert-hex)
      (log/info raw-sign)
      (log/info signature)
      (log/info mac))))


(defn message [req]
  (println req))