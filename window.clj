(ns gtd.window
  (:import (javax.swing JDialog JLabel)))

;the idea with sizing the font:
;try and get everything to fit on the screen
;try not to go below a threshold (90?)
;do not go above a threshold (200?)
;fit to width with nice margin
(defn create-label [message]
  (let [lbl (JLabel. message JLabel/CENTER)]
    (.setFont lbl (.deriveFont (.getFont lbl) (float 200)))
    lbl))

(defn create-window [message]
  (doto (JDialog.)
    (.add (create-label message))
    (.setSize 1500 400) ;TODO: needs to be sized dynamically (width based on screen, height based on font)
    (.setLocation 50 400) ;TODO: needs to be positioned dynamically
    (.setVisible true)
    (.toFront)))

(create-window "Hello")
(create-window "This is a really long sentence that should wrap and display in a smaller font.")
(create-window "07590 719599")
