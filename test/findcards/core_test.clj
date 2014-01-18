(ns findcards.core-test
  (:require [clojure.test :refer :all]
            [findcards.core :refer :all])
  (:import []org.opencv.highgui Highgui]
           [org.opencv.core Size]))

(deftest opencv-binding-test 
  (testing "open the example file using opencv's java bindings"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")]
    (is (= 3 (.channels single)))
    (is (= 1944.0 (.height (.size single))))
    (is (= 2592.0 (.width (.size single)))))))

(deftest mat-scale-test
  (testing "fit to width"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")]
    (is (= 480.0 (.height (.size (mat-scale single 480)))))))) 

(deftest gaussian-blur-test 
  (testing "blur should uhhhhh "
    (let [single (Highgui/imread "resources/examples/one_single.jpg")]
    (is (= 3 (.channels (gaussian-blur single 11 11)))))))

(deftest grayscale-test 
  (testing "grayscale should convert an image to a single channel"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")]
    (is (= 1 (.channels (grayscale single)))))))

(deftest threshold-test 
  (testing "threshold should convert an image to a single channel"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")
          gray (grayscale single)]
    (is (= 1 (.channels (threshold gray 7 10)))))))

(deftest canny-test 
  (testing "canny should convert an image to a single channel"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")]
    (is (= 1 (.channels (canny single 100 700)))))))

(deftest find-external-contours-test 
  (testing "find-external-contours should return a list of matrix of points"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")]
    (is (= org.opencv.core.MatOfPoint (class (first (find-external-contours (canny single 100 700)))))))))

(deftest find-contours-test
  (testing "find-contours should return a list of coutours"
    (let [four (Highgui/imread "resources/examples/four_single.jpg")
          four-edges (canny four 100 700)]
      (is (> (count (find-contours four-edges)) 1)))))

(deftest find-contours-min-area-test
  (testing "find-contours should return a list of coutours"
    (let [four (Highgui/imread "resources/examples/four_single.jpg")
          four-edges (canny four 100 700)]
      (is (= (count (find-contours-min-area four-edges 0.05 0.08)) 1)))))
  
(deftest bounding-rect-test
  (testing "bounding-rect should return a RotatedRect"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")
          contour (first (find-external-contours (canny single 100 700)))]
      (is (= org.opencv.core.RotatedRect (class (bounding-rect contour)))))))
  
(deftest approx-poly-test
  (testing "approxPoly should return MatOfPoint2f"
    (let [single (Highgui/imread "resources/examples/one_single.jpg")
          contour (first (find-external-contours (canny single 100 700)))]
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
  (is (= 1 (count (find-cards (Highgui/imread "resources/examples/one_single.jpg"))))))

(deftest normalize-test
  (let [single (Highgui/imread "resources/examples/one_single.jpg")]
    (is (= (Size. 264 350) (.size (normalize single (first (find-cards single))))))))