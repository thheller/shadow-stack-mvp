(ns repl
  (:require
    ;; optional, remove if you are going to use any kind of JVM logging
    [shadow.cljs.silence-default-loggers]
    [build]
    [clojure.java.io :as io]
    [clojure.main :as main]
    [shadow.user]
    [shadow.cljs.devtools.api :as shadow]
    [shadow.cljs.devtools.server :as server]
    [shadow.cljs.devtools.server.fs-watch :as fs-watch]))

(defonce css-watch-ref (atom nil))

(defn start []
  (server/start!)

  ;; optional, could also do this from the UI
  (shadow/watch :app)

  ;; build css once on start
  (build/css-release)

  ;; the watcher that rebuilds css everything on change
  (reset! css-watch-ref
    (fs-watch/start
      {}
      [(io/file "src" "main")]
      ["cljs" "cljc" "clj"]
      (fn [_]
        (try
          (build/css-release)
          (catch Exception e
            (prn [:css-failed e]))))))

  ::started)

(defn stop []
  (when-some [css-watch @css-watch-ref]
    (fs-watch/stop css-watch))

  ::stopped)

(defn go []
  (stop)
  (start))

(defn repl-init []
  (in-ns 'shadow.user))

(defn -main [& args]
  (start)
  (main/repl :init repl-init))