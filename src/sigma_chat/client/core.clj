(ns sigma-chat.client.core
  (:gen-class)
  (:require [sigma-chat.server.core :as server]
            [sigma-chat.common.utils :as utils]
            [sigma-chat.common.properties :as props]
            [clojure.tools.logging :as log]
            [caesium.crypto.scalarmult :as sm]
            [caesium.crypto.box :as box]
            [caesium.byte-bufs :as bb]
            [clj-http.client :as client]))

(def vault (atom {}))

(defn gen-keys
  "Generates a private and public key using caesium box.

  Keys stored in vault under sm-keys as secret and public. Human readable hex values are
  stored as secret-prt and public-prt. If a seed is passed, it is also stored
  in the vault as seed and seed-prt under sm-keys for the raw and hex values respectively."
  ([]
    (let [keys (box/keypair!)
          secret-prt (utils/byte-buf->hex-string (get keys :secret))
          public-prt (utils/byte-buf->hex-string (get keys :public))
          keys (assoc keys :secret-prt secret-prt :public-prt public-prt)]
      (swap! vault assoc :sm-keys keys)
      (log/info "CLIENT: Generated secret key: " secret-prt)
      (log/info "CLIENT: Generated public key: " public-prt)))
  ([seed]
    (let [keys (box/keypair! seed)
          secret-prt (utils/byte-buf->hex-string (get keys :secret))
          public-prt (utils/byte-buf->hex-string (get keys :public))
          seed (bb/->indirect-byte-buf seed)
          seed-prt (utils/byte-buf->hex-string seed)
          keys (assoc keys :secret-prt secret-prt :public-prt public-prt
                           :seed seed :seed-prt seed-prt)]
      (swap! vault assoc :sm-keys keys)
      (log/info "CLIENT: Generated secret key with seed [" seed-prt "]: " secret-prt)
      (log/info "CLIENT: Generated public key with seed [" seed-prt "]: " public-prt))))


(defn init-seq
  "Initiates the startup sequence for the Sign-and-Mac with the passed server params.

  The function expects a map with the following keys :host :port :route and :key. These keys will
  default to '0.0.0.0', 4568, '/init', and 'BEEFBEEFBEEFBEEF' if the keys are nil.
  The function sends a GET request with key appended to the end of the route."
  [{:keys [host port route key], :or {host "0.0.0.0" port 4568 route "/init" key "BEEFBEEFBEEFBEEF"}}]
  (let [request (str "http://" host ":" port "/" route)
        response (client/post request {:body key})]
    (log/info "CLIENT: Initiating SIGMA sequence: " request)
    (log/info "CLIENT: Received response from SIGMA initiation: " response)))

(defn main []
  (gen-keys)
  (init-seq {:host "0.0.0.0"
             :port 4568
             :route "/init"
             :key ;(utils/buffer->array
                    (get-in @vault [:sm-keys :public-prt])}))

;(defn to-buffer [string]
;  (let [bytes (.getBytes string)
;        buffer (java.nio.ByteBuffer/allocate (count bytes))]
;    (.put buffer bytes)
;    buffer))
;
;(defn to-string [buffer]
;  (reduce
;    (fn [one two]
;      (str one two))
;    (.asCharBuffer buffer)))