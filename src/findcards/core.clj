(ns findcards.core
  (:require [seesaw.core :as ss])
  (:import [org.opencv.imgproc Imgproc]
 		   [org.opencv.highgui Highgui]
           [org.opencv.core Mat MatOfByte MatOfPoint MatOfPoint2f Size CvType Scalar Point Core]
           [javax.imageio ImageIO]))


(defn draw-raw! [^Mat matrix-img]
  (let [mob (MatOfByte.)]
    (Highgui/imencode ".jpg" matrix-img mob)
    (let [buf-img (ImageIO/read (java.io.ByteArrayInputStream. (.toArray mob)))
          panel (ss/grid-panel :paint (fn [c g] (.drawImage g buf-img 0 0 nil)))
          f (ss/frame :title "Image Viewer" :size [640 :by 480] :content panel)]
      (-> f ss/show!))))

(defn scale [src height]
	(let [dest (Mat.)
     	  scale (/ height (.height src))] 
 		(Imgproc/resize src dest (Size.) scale scale Imgproc/INTER_AREA)
   	dest))

(defn draw! [image]
  (draw-raw! (scale image 480)))

(defn grayscale [src]
	(let [dest (Mat. (.size src) CvType/CV_8SC1)]
 		(Imgproc/cvtColor src dest Imgproc/COLOR_RGB2GRAY)	
    dest))

(defn gaussian-blur [src square-size sigma-x]
  (let [dest (Mat. (.size src) (.type src))] 
    (Imgproc/GaussianBlur src dest (Size. square-size square-size) sigma-x)
    dest))

(defn canny [src threshold-low threshold-high]
  (let [dest (Mat. (.size src) (.type src))] 
    (Imgproc/Canny src dest threshold-low threshold-high)
    dest))

(defn threshold [src block-size threshold]
	(let [dest (Mat. (.size src) (.type src))] 
    (Imgproc/adaptiveThreshold src dest 
                               255 Imgproc/ADAPTIVE_THRESH_GAUSSIAN_C 
                               Imgproc/THRESH_BINARY_INV block-size threshold)
    dest))

(defn find-external-contour [src]
  (let [contours (java.util.LinkedList. )]
    (Imgproc/findContours src contours (Mat.) Imgproc/RETR_EXTERNAL Imgproc/CHAIN_APPROX_SIMPLE)
    (first contours)))

(defn draw-contours! [image contour-list]
  (let [copy (.clone image)]
    (Imgproc/drawContours copy contour-list -1 (Scalar. 255 0 0) 5)
    (draw! copy)))

(defn to-float-points [points]
  (let [dest (Mat.)]
    (.convertTo points dest CvType/CV_32FC2)
    dest))

(defn bounding-rect [contour] 
  (Imgproc/minAreaRect (MatOfPoint2f. (to-float-points contour))))

(defn rotated-rect-to-points [rotated-rect]
  (let [points (into-array Point (repeat 4 (Point.) ))]
    (.points rotated-rect points)
    points))

(defn affine-matrix-unrotate-rect [rotated-rect]
  (Imgproc/getRotationMatrix2D (.center rotated-rect) (.angle rotated-rect) 1.0))

(defn unrotate-image [src rotated-rect]
  (let [dest (Mat. (.size src) (.type src))
        affineMatrix (affine-matrix-unrotate-rect rotated-rect)]
    (Imgproc/warpAffine src dest affineMatrix (.size dest))
    dest))

(defn unrotate-rect [rotated-rect]
  (let [copy (.clone rotated-rect)]
    (set! (.angle copy) 0)
    (.boundingRect copy)))

(defn crop [src rotated-rect]
  (Mat. (unrotate-image src rotated-rect) (unrotate-rect rotated-rect)))