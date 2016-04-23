(defproject findcards "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [net.mikera/vectorz-clj "0.18.0"]
                 [opencv/opencv "3.1.0"]
                 [seesaw "1.4.4"]]
  :plugins [[lein-localrepo "0.5.2"]]
  :injections [(clojure.lang.RT/loadLibrary org.opencv.core.Core/NATIVE_LIBRARY_NAME)]
  :require findcards.core
  :repl-options {:init-ns findcards.core})
