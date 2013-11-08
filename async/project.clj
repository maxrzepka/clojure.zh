(defproject zh-async "0.1.0-SNAPSHOT"
  :description "demo of core.async"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.6.0-alpha2"]
                 [org.clojure/clojurescript "0.0-2014"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]]
  :source-paths ["src/clj"]
  :plugins [[lein-cljsbuild "1.0.0-alpha1"]]

  :cljsbuild
  {:builds
   [{:id "hs-async"
     :source-paths ["src/cljs"]
     :compiler {:optimizations :none
                :pretty-print false
                :output-dir "out"
                :output-to "main.js"}}]})
