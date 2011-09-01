(ns gtd.window
  (:import (javax.swing JDialog JLabel)))

;intended exports:
  ;create-window

(defn drop-while-not [f coll]
  (drop-while (complement f) coll))

(defn get-new-lbl-font
  "Derive a newly sized font from given label's current font."
  [lbl size]
  (.deriveFont (.getFont lbl) (float size)))

(defn string-width-in-label
  "Pixel width of string using label's current font."
  [lbl font message]
  (if-let [fm (. lbl (getFontMetrics font))]
    (. fm (stringWidth message))))

(defn next-font-size-delta
  "Delta used to generate next font size based on constraints."
  [lbl font msg max-width]
  (let [width (string-width-in-label lbl font msg)
        size (.getSize font)]
    (cond
      (> size 300) 0
      (< width max-width) 5
      (and (> width max-width) (<= size 90)) -5
      (> width max-width) 0)))

(defn next-font-size
  "Generator function to iterate to font size."
  [lbl message max-width]
  (fn [last-size]
    (let [font (get-new-lbl-font lbl last-size)
          delta (next-font-size-delta lbl font message max-width)]
      (+ last-size delta))))

(defn select-font-size
  "Select font size to use from collection of generated sizes."
  [sizes]
  (float
    (ffirst
      (drop-while-not
        #(or (= (first %) (second %))
             (= (first %) (second (rest %))))
        (partition 3 1 sizes)))))

(defn label-font
  "Given max-width generate the label's font."
  [lbl message max-width]
  (let [next-fs-gen (next-font-size lbl message max-width)
        sizes (iterate next-fs-gen 90)]
    (get-new-lbl-font lbl (select-font-size sizes))))

(defn create-label
  "Create a label for the message."
  [message max-width]
  (let [lbl (JLabel. message JLabel/CENTER)]
    (.setFont lbl (label-font lbl message max-width))
    lbl))

(defn create-window [message]
  "Create the window displaying the message."
  (doto (JDialog.)
    (.add (create-label message (- 1500 100)))
    (.setSize 1500 400)
    (.setLocation 50 400)
    (.setVisible true)
    (.toFront)))

(create-window "Hello")
(create-window "This is a really long sentence that should wrap and display in a smaller font. This is a really long sentence that should wrap and display in a smaller font.")
(create-window "07590 719599")
