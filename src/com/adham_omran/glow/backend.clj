(ns com.adham-omran.glow.backend
  (:require
   [cheshire.core :as json]
   [clojure.pprint :as pprint]
   [com.adham-omran.glow.pages :as pages]
   [hiccup2.core :as h]
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.malli]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.adapter.jetty :as adapter]
   [scicloj.clay.v2.item :as item]
   [tablecloth.api :as tc]))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get {:handler (fn [_] {:body (pages/index-page)})}}]
     ["/api"
      ["/input" {:post
                 {:handler
                  (fn [{{:strs [value]} :form-params}]
                    (pprint/pprint value)
                    {:body
                     (-> [:script
                          (h/raw (format "vegaEmbed(document.currentScript.parentElement,%s).catch(console.error); "
                                         (json/generate-string
                                          {"data" {"values" [{"category" "A", "group" "x", "value" value}
                                                             {"category" "B", "group" "z", "value" 1.1}
                                                             {"category" "C", "group" "z", "value" 0.2}]}

                                           "mark" "bar",
                                           "encoding" {"x" {"field" "category"},
                                                       "y" {"field" "value", "type" "quantitative"},
                                                       "xOffset" {"field" "group"},
                                                       "color" {"field" "group"}}})))]
                         h/html
                         str)})}}]
      ["/world-phones"
       {:post
        {:handler
         (fn [{{:strs [value]} :form-params}]
           {:body
            (-> [:script
                 (h/raw
                  (second
                   (second
                    (:hiccup (item/vega-embed {:value {:data {:values (-> (tc/dataset "./resources/data/worldphones.csv")
                                                                          (tc/select-columns ["Year" value])
                                                                          (tc/rows :as-maps)
                                                                          vec)}
                                                       :mark :bar,
                                                       :encoding {:x {:field "Year"},
                                                                  :y {:field value
                                                                      :type :quantitative}}}})))))]
                h/html
                str)})}}]]

     ["/public/*" (ring/create-resource-handler)]]

    {:data {#_:exception #_pretty/exception
            :coercion reitit.coercion.malli/coercion
            :muuntaja m/instance
            #_:conflicts #_(constantly nil)
            :middleware [parameters/parameters-middleware
                         coercion/coerce-request-middleware]}})
   (ring/routes (ring/create-default-handler))))

(comment
  (def server
    (adapter/run-jetty #'app {:port 8090
                              :join? false}))
  (.stop server))
