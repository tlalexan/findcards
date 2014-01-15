# findcards

Looks for cards in an image


bash% lein repl

findcards.core=> (def single (Highgui/imread "resources/examples/single_card_table.jpg"))
findcards.core=> (def contour (find-external-contour (canny single 100 700)))
findcards.core=> (def rect (bounding-rect contour))
findcards.core=> (draw! (crop single rect))

