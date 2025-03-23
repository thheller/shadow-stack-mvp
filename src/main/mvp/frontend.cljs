(ns mvp.frontend
  (:require
    [shadow.grove :as sg :refer (css defc deftx <<)]
    [shadow.grove.kv :as kv]))

;; a basic event handler
(deftx inc! [id]
  [tx-env ev e]
  (update-in tx-env [:counter id :count] inc))

(defc ui-counter [id]
  (bind val
    (sg/kv-lookup :counter id :count))

  (render
    (<< [:div (css :text-center :pb-6)
         [:button
          (css :border :shadow :py-2 :px-8)
          {:on-click (inc! id)}
          (str "click me: " val)]])))

(def $ui-root
  ;; example css for root element
  ;; define css via tailwind style aliases
  (css :text-5xl :py-8 :font-bold
    ;; or just CSS directly
    {:color "red"
     :text-align "center"}
    ;; pseudo-class support
    [:hover :text-green-700]))

(defc ui-root []
  (bind who
    (sg/kv-lookup :db :who))

  (bind counters
    (sg/kv-lookup :db :counters))

  (render
    (<< [:div {:class $ui-root}
         (str "Hello " who "!")]

        (sg/simple-seq counters ui-counter))))

;; the grove app state
(def app (sg/get-runtime :app))

(defn render []
  (sg/render app js/document.body
    ;; renders our app into the HTML <body>
    (ui-root)))

;; called once on page load via :init-fn in build config
(defn init []
  (sg/add-kv-table app :db {}
    {:who "World"})

  (sg/add-kv-table app :counter
    {:primary-key :id}
    {})

  (sg/kv-init app
    (fn [db]
      (-> db
          ;; merge-seq is helper to add and normalize data to db
          (kv/merge-seq :counter
            ;; setup some dummy counters
            [{:id 1 :count 0}
             {:id 2 :count 5}]
            ;; store generated vector in :db :counters
            [:db :counters]))))

  (render))

;; called after every hot-reload
(defn ^:dev/after-load reload! []
  (render))