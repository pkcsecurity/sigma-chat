(ns sigma-chat.server.core
  (:gen-class)
  (:require [immutant.web :as server]
            [ring.middleware.json :as json]
            [ring.middleware.file :as file]
            [ring.middleware.params :as params]
            [ring.middleware.keyword-params :as kw-params]
            [ring.middleware.content-type :as content-type]
            [sigma-chat.server.routes :as r]
            [sigma-chat.server.roles :as roles]
            [sigma-chat.common.spec :as spec]
            [sigma-chat.common.utils :as utils]
            [sigma-chat.common.properties :as props]
            [sigma-chat.server.sigma :as logic]
            [caesium.crypto.sign :as sign]))

(defn wrap-ignore-trailing-slash [handler]
  (fn [request]
    (let [uri (:uri request)]
      (handler (assoc request
                 :uri
                 (if (and
                       (.endsWith uri "/")
                       (not= "/" uri))
                   (subs uri 0 (dec (count uri)))
                   uri))))))

(def app
  (-> r/routes
    (json/wrap-json-response)
    (json/wrap-json-body {:keywords? true})
    (roles/wrap-security)
    (file/wrap-file "static" {:index-files? false})
    (content-type/wrap-content-type)
    (kw-params/wrap-keyword-params)
    (params/wrap-params)
    (wrap-ignore-trailing-slash)
    (spec/wrap-conform-failure)))

(defn start [& args]
  "Starts a server with host and port defined in sigma_chat/common/properties file.

  Generates the Certificate Authority's private and public keys. These are stored in
  the properties file under ca as :server-public-key.

  The private key is stored in the vault of sigma_chat/server/sigma:vault as ca -> private-key."
  (let [keys (sign/keypair!)
        private (get keys :secert)
        public (get keys :public)]
    (dosync
      (alter props/properties assoc :ca (assoc (get @props/properties :ca) :server-public-key public)))
    (swap! logic/vault assoc :ca keys))

  (if (= (props/get-prop :common :mode) "PROD")
    (server/run app :host (props/get-prop :server :host) :port (props/get-prop :server :port))
    (server/run-dmc app :host(props/get-prop :server :host) :port (props/get-prop :server :port))))