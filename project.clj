(defproject clj-brokee "0.1.5-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.5.527"]
                 [com.taoensso/sente "1.13.1"]]
  :source-paths ["src/clj" "src/cljc"]
  :repl-options {:init-ns clj-brokee.broker})
