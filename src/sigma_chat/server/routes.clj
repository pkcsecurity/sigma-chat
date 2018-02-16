(ns sigma-chat.server.routes
  (:require [compojure.core :as r]
            [compojure.route :as croute]
            [sigma-chat.server.sigma :as logic]))

(r/defroutes routes
  (r/POST "/init" [] logic/init)
  (r/POST "/init/:seed" [seed] logic/init)
  (r/POST "/message" [] logic/message)
  (croute/not-found nil))
