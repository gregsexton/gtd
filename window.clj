(ns gtd.window
  (:import
     (javax.swing JDialog JLabel Box)))

;intended exports:
  ;create-window

(def MIN-FONT-SIZE 20)
(def MAX-FONT-SIZE 300)

(def SCREEN-WIDTH 1600)
(def SCREEN-HEIGHT 1200)
(def SCREEN-MARGIN 100)

(def MAX-WIN-WIDTH (- SCREEN-WIDTH SCREEN-MARGIN))
(def MAX-WIN-HEIGHT (- SCREEN-HEIGHT SCREEN-MARGIN))
(def MIN-WIN-HEIGHT 150)
(def WIN-MARGIN 100)

(def MAX-LBL-WIDTH (- MAX-WIN-WIDTH WIN-MARGIN))
(def MAX-LBL-HEIGHT (- MAX-WIN-HEIGHT WIN-MARGIN))

;utilities
(defn drop-while-not [f coll]
  (drop-while (complement f) coll))
(defn lines [str-input]
  (seq (.split #"\n" str-input)))

;labels
(defn get-new-lbl-font
  "Derive a newly sized font from given label's current font."
  [lbl size]
  (.deriveFont (.getFont lbl) (float size)))

(defn string-width-in-label
  "Pixel width of string using label's current font."
  [lbl font message]
  (if-let [fm (. lbl (getFontMetrics font))]
    (. fm (stringWidth message))))

(defn string-height-in-label
  "Pixel height of any string using the font in the label."
  [lbl font]
  (.. lbl (getFontMetrics font) getHeight))

(defn next-font-size-delta
  "Generate next font size delta based on constraints."
  [lbl font msg max-height]
  (let [width (string-width-in-label lbl font msg)
        height (string-height-in-label lbl font)]
    (cond
      (> height max-height) 0
      (< width MAX-LBL-WIDTH) 5
      (> width MAX-LBL-WIDTH) 0)))

(defn next-font-size
  "Generator function to iterate to font size."
  [lbl message max-height]
  (fn [last-size]
    (let [font (get-new-lbl-font lbl last-size)
          delta (next-font-size-delta lbl font message max-height)]
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
  "Generate the label's font."
  [lbl message max-height]
  (let [next-fs-gen (next-font-size lbl message max-height)
        sizes (iterate next-fs-gen MIN-FONT-SIZE)]
    (get-new-lbl-font lbl (select-font-size sizes))))

(defn create-label
  "Create a label for the message."
  [center? max-height message]
  (let [lbl (JLabel. message (if center? JLabel/CENTER JLabel/LEFT))]
    (.setFont lbl (label-font lbl message max-height))
    (.setAlignmentX lbl (if center? 0.5 0.0))
    lbl))

(defn max-height
  "The maximum height of a label based on number of labels needed and
  screen height."
  [cnt]
  (let [height (/ MAX-LBL-HEIGHT cnt)]
    (cond
      (< height MIN-FONT-SIZE) MIN-FONT-SIZE
      (> height MAX-FONT-SIZE) MAX-FONT-SIZE
      :else height)))

(defn create-labels
  "Create a label for each message."
  [messages]
  (let [msg-cnt (count messages)]
    (map (partial create-label (= msg-cnt 1)
                               (max-height msg-cnt))
         messages)))

;window
(defn center-window [win]
  (.setLocation win
    (/ (- SCREEN-WIDTH (.getWidth win)) 2)
    (/ (- SCREEN-HEIGHT (.getHeight win)) 2)))

(defn get-win-height [layout-manager]
  (let [height (.. layout-manager getPreferredSize getHeight)]
    (cond
      (< height MIN-WIN-HEIGHT) MIN-WIN-HEIGHT
      (> height MAX-WIN-HEIGHT) MAX-WIN-HEIGHT
      :else (+ height 100))))

(defn size-window [win layout-manager]
  (.setSize win MAX-WIN-WIDTH (get-win-height layout-manager)))

(defn make-visible [win]
  (doto win
    (.setVisible true)
    (.toFront)))

(defn create-initial-win
  "Create a default window of default size."
  []
  (doto (JDialog.)
    (.setSize MAX-WIN-WIDTH MAX-WIN-HEIGHT)))

(defn create-window-with-labels
  "Create a default window, add all the labels, resize, center and
  display."
  [labels]
  (let [win (create-initial-win)
        box (Box/createVerticalBox)
        lbl-count (count labels)]
    (.add win box)
    (.add box (Box/createVerticalGlue))
    (doseq [lbl labels]
      (.add box lbl)
      (if (> lbl-count 1) (.add box (Box/createHorizontalStrut (/ WIN-MARGIN 2))))
      (.add box (Box/createVerticalGlue)))
    (.validate win)
    (size-window win box)
    (center-window win)
    (make-visible win)))

(defn create-window [message]
  "Create the window displaying the message."
  (create-window-with-labels
    (create-labels (lines message))))

;doesn't realise full width;
(create-window "Hello")
;honours line breaks and left aligns:
(create-window "for(int i=0; i<10; i++){\n\ti-=1;\ng++;\ng++;\ng++;\ng++;\ng++;\n}")
;breaks and justifies:
(create-window "This is a really long sentence that should wrap and display in a smaller font. This is a really long sentence that should wrap and display in a smaller font.")
;realises full width but doesn't break:
(create-window "07590 719599")
