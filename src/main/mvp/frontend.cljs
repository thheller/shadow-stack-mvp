(ns mvp.frontend
  (:require
    [shadow.grove :as sg :refer (css defc <<)]
    [shadow.grove.db :as db]))

;; our grove app
(def app (sg/get-runtime :app))

;; web animations helper for fun
(def shake
  (sg/prepare-animation
    {:transform ["translateX(0)" "translateX(-10px)" "translateX(10px)" "translateX(0)"]}
    250))

;; a basic event handler
(sg/reg-event app ::inc!
  (fn [env {:keys [id] :as ev}]
    (update-in env [:db id :count] inc)))

(defc ui-counter [id]
  (bind val
    (sg/db-read [id :count]))

  (event ::inc! [env ev dom-event]
    ;; regular event handler shouldn't do any DOM related things
    ;; so do that here first
    (shake (.-target dom-event))
    ;; and then let regular event handler do actual db update
    (sg/run-tx env ev))

  (render
    (<< [:div {:class (css :text-center :pb-6)}
         [:button
          {:class (css :border :shadow :py-2 :px-8)
           :on-click {:e ::inc! :id id}}
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
    (sg/db-read :who))

  (bind counters
    (sg/db-read :counters))

  (render
    (<< [:div {:class $ui-root}
         (str "Hello " who "!")]

        (sg/simple-seq counters ui-counter))))

(defn render []
  (sg/render app js/document.body
    ;; renders our app into the HTML <body>
    (ui-root)))

;; called once on page load via :init-fn in build config
(defn init []
  ;; db type info optional
  ;; but required for db normalization which we'll use in this example
  ;; this can only be done in init and before any data is added to db
  (sg/add-db-type app :counter {:primary-key :id})

  (sg/db-init app
    (fn [db]
      (-> db
          (assoc :who "World")
          ;; merge-seq is helper to add and normalize data to db
          (db/merge-seq :counter
            ;; setup some dummy counters
            [{:id 1 :count 0}
             {:id 2 :count 5}]
            ;; store generated idents in db :counters
            [:counters]))))

  (render))

;; called after every hot-reload
(defn ^:dev/after-load reload! []
  (render))