(ns findcards.core-test
  (:require [clojure.test :refer :all]
            [findcards.core :refer :all])
  (:import [org.opencv.imgcodecs Imgcodecs]
           [org.opencv.core Size]))

(deftest opencv-binding-test 
  (testing "open the example file using opencv's java bindings"
    (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")]
    (is (= 3 (.channels single)))
    (is (= 4160.0 (.height (.size single))))
    (is (= 3120.0 (.width (.size single)))))))

(deftest mat-scale-test
  (testing "fit to width"
    (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")]
    (is (= 480.0 (.height (.size (mat-scale single 480)))))))) 

(deftest grayscale-test 
  (testing "grayscale should convert an image to a single channel"
    (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")]
    (is (= 1 (.channels (grayscale single)))))))

(deftest threshold-test 
  (testing "threshold should convert an image to a single channel"
    (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")
          gray (grayscale single)]
    (is (= 1 (.channels (threshold gray 7 10)))))))

(deftest find-contours-test
  (testing "find-contours should return a list of coutours"
    (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")
          single-edges (dilate (threshold (grayscale single) 123 10) 8)]
      (is (> (count (find-contours single-edges)) 1)))))

(deftest find-contours-min-area-test
  (testing "find-contours should return a list of coutours"
    (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")
          single-edges (dilate (threshold (grayscale single) 123 10) 8)]
      (is (= (count (find-contours-min-area single-edges 0.05 0.20)) 1)))))
  
(deftest approx-poly-test
  (testing "approxPoly should return MatOfPoint2f"
    (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")
          single-edges (dilate (threshold (grayscale single) 123 10) 8)
          contour (first (find-contours single-edges))]
      (is (= org.opencv.core.MatOfPoint2f (class (approx-poly contour)))))))

(deftest edges-test
  (is (= [ [ [0 0] [1 1] ] 
           [ [1 1] [0 0] ] ] (edges [ [0 0] [1 1] ] ) )))

(deftest length-test
  (is (= 5.0 (length [[2, -1] [-2, 2]]))))

(deftest longest-edge-first-test
  (is (= [[0, 1] [10, 1] [9, 0] [0,0]] (longest-edge-first [[0, 0] [0, 1] [10, 1] [9, 0]]))))

(deftest clockwise?-test
  (is (clockwise? [[0, 0] [0, 1] [10, 1] [9, 0]]))
  (is (not (clockwise? (reverse [[0, 0] [0, 1] [10, 1] [9, 0]]))))
  (testing "two points is clockwise"
    (is (clockwise? [[0, 0] [0, 1]]))))

(deftest clockwise-test
  (is (= [[0, 0] [0, 1] [10, 1] [9, 0]] (clockwise (reverse [[0, 0] [0, 1] [10, 1] [9, 0]])))))

(deftest find-cards-test
  (testing "single_set_card has one card"
    (is (<= 1 (count (find-cards (Imgcodecs/imread "resources/examples/single_set_card.jpg"))))))
  (testing "four_set_cards_on_messy_desk has four cards"
    (is (= 4 (count (find-cards (Imgcodecs/imread "resources/examples/four_set_cards_on_messy_desk.jpg"))))))
  (testing "twelve_set_cards has twelve cards"
    (is (<= 12 (count (find-cards (Imgcodecs/imread "resources/examples/twelve_set_cards.jpg"))))))
  (testing "another_twelve_set_cards has more than 9 cards"
    (is (<= 9 (count (find-cards (Imgcodecs/imread "resources/examples/another_twelve_set_cards.jpg")))))))


(deftest normalize-test
  (let [single (Imgcodecs/imread "resources/examples/single_set_card.jpg")]
    (is (= (Size. 264 350) (.size (normalize single (first (find-cards single))))))))