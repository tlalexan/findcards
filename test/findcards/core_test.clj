(ns findcards.core-test
  (:require [clojure.test :refer :all]
            [findcards.core :refer :all])
  (:import (org.opencv.highgui Highgui)))

(deftest opencv-binding-test 
  (testing "open the example file using opencv's java bindings"
    (let [single (Highgui/imread "resources/examples/single_card_table.jpg")]
    (is (= 1944.0 (.height (.size single)))))))
