(ns findcards.core
  (:require [seesaw.core :as ss]
            [seesaw.keymap :as ssk]
            [clojure.core.matrix :as m])
  (:import [org.opencv.imgproc Imgproc ]
           [org.opencv.imgcodecs Imgcodecs]
           [org.opencv.core Mat MatOfByte MatOfPoint MatOfPoint2f Size CvType Scalar Point Core ]
           [javax.imageio ImageIO]))

(m/set-current-implementation :vectorz)

(defn draw-raw! [^Mat matrix-img]
  (let [mob (MatOfByte.)]
    (Imgcodecs/imencode ".jpg" matrix-img mob)
    (let [buf-img (ImageIO/read (java.io.ByteArrayInputStream. (.toArray mob)))
          panel (ss/grid-panel :paint (fn [c g] (.drawImage g buf-img 0 0 nil)))
          f (ss/frame :title "Image Viewer" :size [640 :by 480] :content panel :on-close :dispose)
          key-handler (fn [e] (ss/dispose! f))]
      (ss/listen f :key-pressed key-handler)
      (ss/show! f))))

(defn mat-scale [src height]
  (let [dest (Mat.)
        scale (/ height (.height src))] 
    (Imgproc/resize src dest (Size.) scale scale Imgproc/INTER_AREA)
    dest))

(defn draw! [image]
  (draw-raw! (mat-scale image 480)))

(defn grayscale [src]
  (let [dest (Mat. (.size src) CvType/CV_8SC1)]
    (Imgproc/cvtColor src dest Imgproc/COLOR_RGB2GRAY)  
    dest))


(defn threshold [src block-size threshold]
  (let [dest (Mat. (.size src) (.type src))] 
    (Imgproc/adaptiveThreshold src dest 
                               255 Imgproc/ADAPTIVE_THRESH_GAUSSIAN_C 
                               Imgproc/THRESH_BINARY_INV block-size threshold)
    dest))

(defn dilate 
  ([src iterations] 
     (let [dest (Mat. (.size src) (.type src))] 
      (Imgproc/dilate src dest (Mat.) (Point. -1 -1) iterations)
      dest))
  ([src] (dilate src 1)))

(defn find-contours [src]
  (let [contours (java.util.LinkedList. )]
    (Imgproc/findContours src contours (Mat.) Imgproc/RETR_LIST Imgproc/CHAIN_APPROX_SIMPLE)
    contours))

(defn find-contours-min-area [src min-area-as-percentage-of-src  max-area-as-percentage-of-src]
  (let [src-area (.area (.size src))]
    (filter 
      (fn [contour] 
        (let [area (/ (.area (Imgproc/boundingRect contour)) src-area)]
          (and (> area min-area-as-percentage-of-src)
               (< area max-area-as-percentage-of-src)))) 
      (find-contours src))))

(defn draw-contours! [image contour-list]
  (let [copy (.clone image)]
    (Imgproc/drawContours copy contour-list -1 (Scalar. 255 0 0) 5)
    (draw! copy)))

(defn approx-poly 
  ([contour] (approx-poly contour 50))
  ([contour epsilon] 
    (let [contour-2f (MatOfPoint2f. (into-array (.toList contour)))
          poly (MatOfPoint2f.)
          closed? true]
      (Imgproc/approxPolyDP contour-2f poly (double epsilon) closed?)
      poly)))

(defn to-seq [points]
  (map (fn [point] [(.x point) (.y point)]) points))

(defn mat-to-seq [^MatOfPoint2f mat] 
  (to-seq (.toList mat)))

(defn to-mat [poly]
  (MatOfPoint. (into-array (map (fn [point] (Point. (double (first point)) (double (second point)))) poly))))

(defn to-mat2f [poly]
  (MatOfPoint2f. (into-array (map (fn [point] (Point. (double (first point)) (double (second point)))) poly))))

(defn as-size [point] (Size. (first point) (second point)))

(defn clockwise? 
  "see https://en.wikipedia.org/wiki/Curve_orientation#Orientation_of_a_simple_polygon" 
  [poly] 
  (if (> (count poly) 2)
    (let [first-three-points (take 3 poly) 
          orientation-matrix (map (fn [point] [1 (first point) (second point)]) first-three-points)]
      (> 0 (m/det orientation-matrix)))    
    true))

(defn clockwise
  "makes a polygon clockwise by reversing its points if needed"
  [poly]
  (if (clockwise? poly)
    poly
    (reverse poly)))

(defn draw-poly! [image & polys]
  (let [copy (.clone image)]
    (Imgproc/polylines copy (map to-mat polys) true (Scalar. 0 0 255) 5)
    (draw! copy)))
  
(defn edges [polygon-points]
  (map (fn [x y] [x y]) polygon-points (m/rotate polygon-points 0 1)))

(defn length[edge]
  (m/distance (first edge) (second edge)))

(defn longest-edge-first [poly]
  (m/rotate poly 0 (.indexOf (edges poly) (last (sort-by length (edges poly))))))

(defn length-first-edge [poly]
  (int (m/distance (first poly) (second poly))))


(defn find-cards 
  ([src dilate-iters]
    (let [four-sided (fn [poly] (= 4 (count poly))) 
          contour-to-poly (fn [contour] (longest-edge-first (clockwise (mat-to-seq (approx-poly contour)))))]
      (filter 
         four-sided 
         (map contour-to-poly (find-contours-min-area (dilate (threshold (grayscale src) 123 10) dilate-iters) 0.02 0.2)))))
  ([src] (find-cards src 12)))

(defn normalize
  ([src long-edge-first-poly normalized-card-height normalized-card-padding card-ratio]
   (let [normalized-card-dimentions [(* card-ratio normalized-card-height) normalized-card-height]
         normalized-card-size (as-size normalized-card-dimentions)
         padded-normalized-card-frame (m/add normalized-card-dimentions (repeat 2 (* 2 normalized-card-padding)))
         normalized-card-poly (let [ origin (repeat 2 normalized-card-padding) ]
                              [ origin
                                (m/add origin [0 (second normalized-card-dimentions)])
                                (m/add origin [(first normalized-card-dimentions) (second normalized-card-dimentions)])
                                (m/add origin [(first normalized-card-dimentions) 0])])
         matrix (Imgproc/getPerspectiveTransform (to-mat2f long-edge-first-poly) (to-mat2f normalized-card-poly))
         padded-normalized-card-frame-size (as-size padded-normalized-card-frame)
         dest (Mat. padded-normalized-card-frame-size (.type src))]
    (Imgproc/warpPerspective src dest matrix (.size dest) Imgproc/INTER_LINEAR Core/BORDER_CONSTANT (Scalar. 0 0 0))
    dest))
  ([src long-edge-first-poly normalized-card-height normalized-card-padding ]
    (normalize src long-edge-first-poly normalized-card-height normalized-card-padding (/ 5 7) ))
  ([src long-edge-first-poly] (normalize src long-edge-first-poly 300 25)))


(comment


(def image (Imgcodecs/imread "resources/examples/another_twelve_set_cards.jpg"))

(draw! (threshold (grayscale image) 123 10) )
(draw! (dilate (threshold (grayscale image) 123 10) 8))

(apply draw-poly! image (find-cards image 12))
(draw! (normalize image (first (find-cards image 12))))

(map-indexed (fn [i p] (Imgcodecs/imwrite (str (+ i 20) ".jpg") (normalize image p))) (find-cards image 12))

)