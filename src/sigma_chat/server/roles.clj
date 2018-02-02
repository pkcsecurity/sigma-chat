(ns sigma-chat.server.roles
  (:require [buddy.auth :as auth]
            [buddy.auth.accessrules :as authz]
            [buddy.auth.backends.token :as token]
            [buddy.auth.middleware :as mw]
            [caesium.crypto.generichash :as crypto]))

(def allow-all (constantly true))
(def deny-all (constantly false))

;TODO: Migrate this to the database
(defonce tokens (atom {}))

(defn store-token [token obj]
  (swap! tokens assoc token obj))

(defn auth-only [req]
  (auth/authenticated? req))

(defn verify-authz [req]
  (case (:request-method req)
    :post (auth-only req)
    :get true
    false))

;TODO Change auth rules
;TODO add auth for making sure a user has a permission
(def rules
  [{:pattern #"^/.*$"
    :handler allow-all}])

;TODO this may be wrong because you have a user name not an email
(defn token-auth-fn [req token]
  (when-let [{:keys [id email]} (get @tokens token)]
    {:sub id
     :email email}))

(def auth-backend
  (token/token-backend {:token-name "Bearer"
                        :authfn token-auth-fn}))

(defn wrap-security [app]
  (-> app
    (authz/wrap-access-rules {:rules rules})
    (mw/wrap-authorization auth-backend)
    (mw/wrap-authentication auth-backend)))
