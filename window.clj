(ns window
  (:use [reflow :only (reflow)])
  (:import
     (javax.swing JDialog JLabel Box)
     (java.awt Color Toolkit)))

;intended exports:
  ;create-window

(def MIN-FONT-SIZE 20)
(def MAX-FONT-SIZE 300)

(def SCREEN-WIDTH (.. Toolkit getDefaultToolkit getScreenSize getWidth))
(def SCREEN-HEIGHT (.. Toolkit getDefaultToolkit getScreenSize getHeight))
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

(defn longest-item
  "Returns the item with longest count in collection coll."
  [coll]
  (defn max-count-item [x y]
    (if (< (count x) (count y)) y x))
  (reduce #(max-count-item %1 %2) [] coll))

(defn longest-count
  "Returns the count of the longest collection in coll."
  [coll]
  (reduce #(max (count %2) %1) 0 coll))

;labels
(defn lbl-font-size
  "Get the label's font size."
  [lbl]
  (.. lbl getFont getSize))

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

(defn next-font-size
  "Generator function used to iterate to desired font size."
  [lbl message max-height]
  (defn next-font-size-delta
    "Generate next font size delta based on constraints."
    [lbl font msg max-height]
    (let [width (string-width-in-label lbl font msg)
          height (string-height-in-label lbl font)]
      (cond
        (> height max-height) 0
        (< width MAX-LBL-WIDTH) 5
        (> width MAX-LBL-WIDTH) 0)))
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
  "Create a label for the message. Fits the label to the MAX-LBL-WIDTH
  and other constraints. Does not perform any sort of reflow."
  [center? max-height message]
  (let [lbl (JLabel. message (if center? JLabel/CENTER JLabel/LEFT))]
    (.setFont lbl (label-font lbl message max-height))
    (.setForeground lbl (Color. 255 255 255))
    (.setAlignmentX lbl (if center? 0.5 0.0))
    lbl))

(defn lbl-max-height
  "The maximum height of a label based on number of labels needed and
  screen height."
  [cnt]
  (let [height (/ MAX-LBL-HEIGHT cnt)]
    (cond
      (< height MIN-FONT-SIZE) MIN-FONT-SIZE
      (> height MAX-FONT-SIZE) MAX-FONT-SIZE
      :else height)))

(defn create-label-seq
  "Create a seq of labels, one for each message in coll."
  [coll center?]
  (let [msg-cnt (count coll)]
    (map (partial create-label center? (lbl-max-height msg-cnt))
         coll)))

(defn create-label-with-flow
  "Creates a seq of labels and is allowed to break (reflow) the message
  as it sees fit."
  [message]
  (defn valid-flow [{:keys [breaks longest-msg]}]
    (> (lbl-font-size
          (create-label true (lbl-max-height breaks) longest-msg))
        MIN-FONT-SIZE))
  (let [breaks-seq  (iterate inc 1)
        longests    (map #(longest-item (reflow message %)) breaks-seq)
        flows       (map #(hash-map :breaks %1 :longest-msg %2) breaks-seq longests)
        valid-flows (drop-while (complement valid-flow) flows)]
    (create-label-seq (reflow message (:breaks (first valid-flows))) true)))

(defn create-labels
  "Create a label for each message. If only one message center justify
  labels and allow reflow. Otherwise honour line breaks and left align."
  [[message & tail :as messages]]
  (if tail
    (create-label-seq messages false)
    (create-label-with-flow message)))

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
      :else (+ height WIN-MARGIN))))

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
    (.setUndecorated true)
    (.setAlwaysOnTop true)
    (.setBackground (Color. 0 81 115 80))
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

;integration tests
(defn integration-tests []
  ;doesn't realise full width;
  (create-window "Hello")
  ;honours line breaks, left aligns and handles tabs:
  (create-window "for(int i=0; i<10; i++){\n\ti-=1;\n\t\tg++;\n\tg++;\n\t\t\tg++;\ng++;\n\tg++;\n}")
  ;breaks and center justifies:
  (create-window "This is a really long sentence that should wrap and display in a smaller font. This is a really long sentence that should wrap and display in a smaller font.")
  ;realises full width but doesn't break:
  (create-window "07590 719599"))

(integration-tests)
