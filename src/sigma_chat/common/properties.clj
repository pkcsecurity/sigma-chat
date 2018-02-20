(ns sigma-chat.common.properties)

(def properties (ref
                  {:server {
                            :host "0.0.0.0"
                            :port 4568}
                   :ca     {:server-public-key "AAAABBBBCCCCDDDDEEEEFFFF"}
                   :arte   {:identity "MEGATRON"}
                   :spencer{:identity "HYPERION"}
                   :common {
                            :mode "DEV"
                            ;:mode "PROD"
                            }}))

(defn get-prop [who prop]
  (get (get @properties who) prop))
