(ns build
  (:require
    [shadow.css.build :as cb]
    [shadow.cljs.devtools.api :as shadow]
    [clojure.java.io :as io]))

(defn css-release [& args]
  (let [build-state
        (-> (cb/start)
            (cb/index-path (io/file "src" "main") {})
            (cb/generate
              '{:main
                {:entries [mvp.frontend]}})
            (cb/minify)
            (cb/write-outputs-to (io/file "public" "css")))]

    (doseq [mod (:outputs build-state)
            {:keys [warning-type] :as warning} (:warnings mod)]
      (prn [:CSS (name warning-type) (dissoc warning :warning-type)]))))

(defn js-release []
  (shadow/release! :app))

(defn all []
  (css-release)
  (js-release)

  :done)

(comment
  (css-release))
