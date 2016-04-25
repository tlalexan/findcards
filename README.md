# findcards

Looks for cards in an image

To use opencv you'll need to install the C shared libraries.  In particular also the java library.  On my box they live at 

You'll also need the opencv jar which isn't distributed from maven central.  This project has a plugin that can install it...  I did ``` lein localrepo install /usr/share/opencv/java/opencv-310.jar opencv/opencv 3.1.0```

See also http://docs.opencv.org/3.1.0/d7/d1e/tutorial_clojure_dev_intro.html


```
tomarchina% ls /usr/lib/*opencv* | grep \.so$
/usr/lib/libopencv_aruco.so
/usr/lib/libopencv_bgsegm.so
/usr/lib/libopencv_bioinspired.so
/usr/lib/libopencv_calib3d.so
/usr/lib/libopencv_ccalib.so
/usr/lib/libopencv_core.so
/usr/lib/libopencv_datasets.so
/usr/lib/libopencv_dnn.so
/usr/lib/libopencv_dpm.so
/usr/lib/libopencv_face.so
/usr/lib/libopencv_features2d.so
/usr/lib/libopencv_flann.so
/usr/lib/libopencv_fuzzy.so
/usr/lib/libopencv_highgui.so
/usr/lib/libopencv_imgcodecs.so
/usr/lib/libopencv_imgproc.so
/usr/lib/libopencv_java310.so
/usr/lib/libopencv_line_descriptor.so
/usr/lib/libopencv_ml.so
/usr/lib/libopencv_objdetect.so
/usr/lib/libopencv_optflow.so
/usr/lib/libopencv_photo.so
/usr/lib/libopencv_plot.so
/usr/lib/libopencv_reg.so
/usr/lib/libopencv_rgbd.so
/usr/lib/libopencv_saliency.so
/usr/lib/libopencv_shape.so
/usr/lib/libopencv_stereo.so
/usr/lib/libopencv_stitching.so
/usr/lib/libopencv_structured_light.so
/usr/lib/libopencv_superres.so
/usr/lib/libopencv_surface_matching.so
/usr/lib/libopencv_text.so
/usr/lib/libopencv_tracking.so
/usr/lib/libopencv_video.so
/usr/lib/libopencv_videoio.so
/usr/lib/libopencv_videostab.so
/usr/lib/libopencv_xfeatures2d.so
/usr/lib/libopencv_ximgproc.so
/usr/lib/libopencv_xobjdetect.so
/usr/lib/libopencv_xphoto.so
```


```
bash% lein repl


(def image (Imgcodecs/imread "resources/examples/another_twelve_set_cards.jpg"))

(draw! (adaptive-threshold (to-grayscale image) 123 10) )
(draw! (dilate (adaptive-threshold (to-grayscale image) 123 10) 8))

(apply draw-poly! image (find-cards image 12))
(draw! (normalize image (first (find-cards image 12))))

(map-indexed (fn [i p] (Imgcodecs/imwrite (str (+ i 20) ".jpg") (normalize image p))) (find-cards image 12))

(def red-squiggle-solid-1 (card-image :red :squiggle :solid 1))
(def red-hist (hue-histogram red-squiggle-solid-1))
(def purple-squiggle-solid-3 (card-image :purple :squiggle :solid 3))
(def purple-hist (hue-histogram purple-squiggle-solid-3))
(def green-squiggle-solid-2 (card-image :green :squiggle :solid 2))
(def green-hist (hue-histogram green-squiggle-solid-2))

(draw-histogram! (crop (card-image :purple :oval :outlined 1) 20))
(draw-histogram! (crop (card-image :purple :squiggle :solid 3) 20))

(card-color green-squiggle-solid-2)


```