(ns findcards.core
  (:require [seesaw.core :as ss])
  (:import [org.opencv.imgproc Imgproc]
 		   [org.opencv.highgui Highgui]
           [org.opencv.core Mat MatOfByte Size CvType Scalar]
           [javax.imageio ImageIO]))


(defn draw-raw! [^Mat matrix-img]
  (let [mob (MatOfByte.)]
    (Highgui/imencode ".jpg" matrix-img mob)
    (let [buf-img (ImageIO/read (java.io.ByteArrayInputStream. (.toArray mob)))
          panel (ss/grid-panel :paint (fn [c g] (.drawImage g buf-img 0 0 nil)))
          f (ss/frame :title "Image Viewer" :size [640 :by 480] :content panel)]
      (-> f ss/show!))))

(defn scale [src width]
	(let [dest (Mat.)
     	  scale (/ width (.width src))] 
 		(Imgproc/resize src dest (Size.) scale scale Imgproc/INTER_AREA)
   	dest))

(defn draw! [image]
  (draw-raw! (scale image 640)))

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

(defn find-contours [src]
  (let [contours (java.util.LinkedList. )]
    (Imgproc/findContours src contours (Mat.) Imgproc/RETR_CCOMP Imgproc/CHAIN_APPROX_SIMPLE)
    contours))

(defn draw-contours! [image contours]
  (let [copy (.clone image)]
    (Imgproc/drawContours copy contours -1 (Scalar. 255 0 0) 5)
    (draw! copy)))


