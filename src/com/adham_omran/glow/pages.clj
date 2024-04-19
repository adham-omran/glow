(ns com.adham-omran.glow.pages
  (:require
   [cheshire.core :as json]
   [hiccup.page :as page]
   [hiccup2.core :as h]
   [tablecloth.api :as tc]
   [scicloj.kindly.v4.kind :as kind]))

(defn index-page
  []
  (page/html5
   {:ng-app "myApp" :lang "en"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "Glow"]
    [:link {:rel "icon"
            :href "data:;base64,iVBORw0KGgo="}]
    [:body
     (page/include-js "./public/htmx.min.js")
     (page/include-js "https://cdn.jsdelivr.net/npm/vega@5")
     (page/include-js "https://cdn.jsdelivr.net/npm/vega-lite@5")
     (page/include-js "https://cdn.jsdelivr.net/npm/vega-embed@6")
     [:div
      {:style {:display "flex"
               :justify-content "center"
               :align-items "center"
               }}
      [:div
       [:div
        [:input
         {:type "range"
          :id "value"
          :name "value"
          :min 0.1
          :max 1.5
          :value 0.7
          :step 0.1
          :hx-trigger "change"
          :hx-post "/api/input"
          :hx-swap "innerhtml"
          :hx-target "#chart"}
         "Slider"]
        (into
         [:select
          {:name "value"
           :hx-trigger "change"
           :hx-post "/api/input"
           :hx-swap "innerhtml"
           :hx-target "#chart"}]
         (map (fn [n]
                (let [n (float n)]
                  [:option {:value n} (str n)]))
              (range 0.1 1 0.1)))]
       [:div#chart
        ;; TODO​ Make this use kind/vega-lite
        [:script
         (h/raw (format "vegaEmbed(document.currentScript.parentElement,%s).catch(console.error); "
                        (json/generate-string
                         {"data" {"values" [{"category" "A", "group" "x", "value" 1.0}
                                            {"category" "B", "group" "z", "value" 1.1}
                                            {"category" "C", "group" "z", "value" 0.2}]}

                          "mark" "bar",
                          "encoding" {"x" {"field" "category"},
                                      "y" {"field" "value", "type" "quantitative"},
                                      "xOffset" {"field" "group"},
                                      "color" {"field" "group"}}})))]]]
      [:div
       {:style {:display "flex"
                :justify-content "center"
                :align-items "center"
                :flex-direction "column"}}
       [:h1 "World Phones"]
       (into
        [:select
         {:name "value"
          :hx-trigger "change"
          :hx-post "/api/world-phones"
          :hx-swap "innerhtml"
          :hx-target "next #chart"}]
        (map (fn [n]
               (let [n n]
                 [:option {:value n} (str n)]))
             (-> "./resources/data/worldphones.csv"
                 tc/dataset
                 tc/column-names
                 rest)))
       [:div
        {:id "chart"}
        [:script
         (h/raw (format "vegaEmbed(document.currentScript.parentElement,%s).catch(console.error); "
                        (json/generate-string
                         {"data" {"values" (-> (tc/dataset "./resources/data/worldphones.csv")
                                               (tc/select-columns ["Year" "Mid-Amer"])
                                               (tc/rows :as-maps)
                                               vec)}
                          "mark" "bar",
                          "encoding" {"x" {:field "Year"},
                                      "y" {:field "Mid-Amer"
                                           :type "quantitative"}}})))]]]
      ]

     ]]))
