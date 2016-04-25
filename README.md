# findcards

Looks for cards in an image

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