(ns findcards.core-test
  (:require [clojure.test :refer :all]
            [findcards.core :refer :all])
  (:import (org.opencv.highgui Highgui)))

(deftest opencv-binding-test 
  (testing "open the example file using opencv's java bindings"
    (let [single (Highgui/imread "resources/examples/single_card_table.jpg")]
    (is (= 3 (.channels single)))
    (is (= 1944.0 (.height (.size single))))
    (is (= 2592.0 (.width (.size single)))))))

(deftest scale-test
  (testing "fit to width"
    (let [single (Highgui/imread "resources/examples/single_card_table.jpg")]
    (is (= 640.0 (.width (.size (scale single 640)))))))) 

(deftest gaussian-blur-test 
  (testing "blur should uhhhhh "
    (let [single (Highgui/imread "resources/examples/single_card_table.jpg")]
    (is (= 3 (.channels (gaussian-blur single 11 11)))))))

(deftest grayscale-test 
  (testing "grayscale should convert an image to a single channel"
    (let [single (Highgui/imread "resources/examples/single_card_table.jpg")]
    (is (= 1 (.channels (grayscale single)))))))

(deftest threshold-test 
  (testing "threshold should convert an image to a single channel"
    (let [single (Highgui/imread "resources/examples/single_card_table.jpg")
          gray (grayscale single)]
    (is (= 1 (.channels (threshold gray 7 10)))))))

(deftest canny-test 
  (testing "canny should convert an image to a single channel"
    (let [single (Highgui/imread "resources/examples/single_card_table.jpg")]
    (is (= 1 (.channels (canny single 500 700)))))))


; (CvType/typeToString (.type single))