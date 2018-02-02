(ns sigma-chat.server.routes
  (:require [compojure.core :as r]
            [compojure.route :as croute]
            [sigma-chat.server.sigma :as logic]))

(r/defroutes routes
  (r/GET "/init/:key" [key] logic/init)
  (r/POST "/message" [] logic/message)
  (croute/not-found nil))
