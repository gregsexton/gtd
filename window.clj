(ns window
  (:use [reflow :only (reflow)])
  (:use [clojure.string :only (split)])
  (:import
     (java.io File)
     (javax.swing JDialog JLabel Box ImageIcon JScrollPane BorderFactory)
     (java.awt.event KeyAdapter KeyEvent MouseAdapter MouseMotionAdapter)
     (com.sun.awt AWTUtilities AWTUtilities$Translucency)
     (java.awt.geom RoundRectangle2D$Double)
     (java.awt Font Color Toolkit Image)))

;intended exports:
  ;create-window

(def MIN-FONT-SIZE 40)
(def MAX-FONT-SIZE 300)

(def SCREEN-WIDTH (.. Toolkit getDefaultToolkit getScreenSize getWidth))
(def SCREEN-HEIGHT (.. Toolkit getDefaultToolkit getScreenSize getHeight))
(def SCREEN-MARGIN 200)

(def MAX-WIN-WIDTH (- SCREEN-WIDTH SCREEN-MARGIN))
(def MAX-WIN-HEIGHT (- SCREEN-HEIGHT SCREEN-MARGIN))
(def MIN-WIN-HEIGHT 150)
(def WIN-MARGIN 100)

(def MAX-LBL-WIDTH (- MAX-WIN-WIDTH WIN-MARGIN))
(def MAX-LBL-HEIGHT (- MAX-WIN-HEIGHT WIN-MARGIN))

;the font size to rise by when iterating toward desired font
(def FONT-INCREMENT 5)

;utilities
(defn drop-while-not [f coll]
  (drop-while (complement f) coll))

(defn lines [str-input]
  (seq (.split #"\n" str-input)))

(defn expand-tabs [str-input]
  (apply str (map #(if (= % \tab) "    " %) str-input)))

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

(defn is-file? [file-path]
  (.exists (File. file-path)))

(defn extension [file-path]
  (last (split file-path #"\.")))

(defn is-image? [file-path]
  (let [ext (.toLowerCase (extension file-path))]
    (and (is-file? file-path)
         (not= "" ext)
         ;bmp not supported by imageicon => not supported by gtd
         (some #{ext} ["jpg" "jpeg" "png" "gif"]))))

;text labels
(defn lbl-font
  "Get the label's font."
  [lbl] (.getFont lbl))

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
  (if-let [fm (.getFontMetrics lbl font)]
    (.stringWidth fm message)))

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
        (< width MAX-LBL-WIDTH) FONT-INCREMENT
        (> width MAX-LBL-WIDTH) 0)))
  (fn [last-size]
    (let [font (get-new-lbl-font lbl last-size)
          delta (next-font-size-delta lbl font message max-height)]
      (+ last-size delta))))

(defn select-font-size
  "Select font size to use from collection of generated sizes."
  [sizes]
  (float
    (- (ffirst
         (drop-while-not
           #(or (= (first %) (second %))
                (= (first %) (second (rest %))))
           (partition 3 1 sizes)))
       FONT-INCREMENT)))

(defn label-font
  "Generate the label's font."
  [lbl message max-height]
  (.setFont lbl (Font. "Arial" Font/BOLD MIN-FONT-SIZE)) ;first time: set font face
  (let [next-fs-gen (next-font-size lbl message max-height)
        sizes (iterate next-fs-gen MIN-FONT-SIZE)]
    (get-new-lbl-font lbl (select-font-size sizes))))

(defn drop-shadow-label [message alignment]
  (proxy [JLabel] [message alignment]
    (paintComponent [g]
      (doto g
        (.setColor (Color. 20 20 20))
        (.drawString message 4 (+ 4 (.getAscent (proxy-super getFontMetrics
                                                             (proxy-super getFont))))))
      (proxy-super paintComponent g))))

(defn create-label
  "Create a label for the message. Fits the label to the MAX-LBL-WIDTH
  and other constraints. Does not perform any sort of reflow. If font is
  not nil then it is used otherwise a best-fit font is chosen."
  [center? max-height message font]
  (let [lbl (drop-shadow-label message (if center? JLabel/CENTER JLabel/LEFT))]
    (.setFont lbl (if (nil? font) (label-font lbl message max-height) font))
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
  "Create a seq of labels, one for each message in coll. If font is nil
  then a best-fit font is chosen."
  [coll center?]
  (let [msg-cnt (count coll)
        font (lbl-font (create-label center? (lbl-max-height msg-cnt) (longest-item coll) nil))]
    (map #(create-label center? (lbl-max-height msg-cnt) % font)
         coll)))

(defn create-label-with-flow
  "Creates a seq of labels and is allowed to break (reflow) the message
  as it sees fit."
  [message]
  (defn valid-flow [{:keys [breaks longest-msg]}]
    (> (lbl-font-size
          (create-label true (lbl-max-height breaks) longest-msg nil))
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

;image labels
(defn get-image [file-path]
  (.getImage (ImageIcon. file-path)))

(defn create-scaled-image-label
  "Creates a label with the given image file path scaled to fit the
  height and width of the window."
  [img]
  (JLabel.
    (ImageIcon.
      (if (> (.getWidth img) (.getHeight img))
        (.getScaledInstance img MAX-LBL-WIDTH -1 Image/SCALE_SMOOTH)
        (.getScaledInstance img -1 MAX-LBL-HEIGHT Image/SCALE_SMOOTH)))))

(defn create-full-size-image-label
  "Creates a label with the given image file path at full size."
  [img]
  (JLabel. (ImageIcon. img)))

(defn create-image-label 
  "Create an image label sized appropriately."
  [file-path] 
  (let [img (get-image file-path)] 
    (if (or (> (.getWidth img) MAX-LBL-WIDTH)
            (> (.getHeight img) MAX-LBL-HEIGHT)) 
      (create-scaled-image-label img) 
      (create-full-size-image-label img))))

;listeners
(defn close-window-key-listener [win]
  (proxy [KeyAdapter] []
    (keyPressed [e]
      (when (= (.getKeyCode e) KeyEvent/VK_ESCAPE)
        (.dispose win)))))

(def *initial-point* (atom nil))

(defn set-initial-click-listener []
  (proxy [MouseAdapter] []
    (mousePressed [e]
      (reset! *initial-point* (.getPoint e)))))

(defn move-window-mouse-listener [win]
  (proxy [MouseMotionAdapter] []
    (mouseDragged [e]
      (if-let [initial-point @*initial-point*]
        (let [x-moved (- (.getX e) (.x initial-point))
              y-moved (- (.getY e) (.y initial-point))]
          (.setLocation win
                        (+ (.. win getLocation x) x-moved)
                        (+ (.. win getLocation y) y-moved)))))))

(defn add-win-listeners 
  ([win]
   (add-win-listeners win win))
  ([win top-component]
   (.addKeyListener win (close-window-key-listener win)) 
   (.addMouseListener top-component (set-initial-click-listener)) 
   (.addMouseMotionListener top-component (move-window-mouse-listener win))))

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

(defn make-win-translucent [win opacity]
  (if (and (AWTUtilities/isTranslucencySupported AWTUtilities$Translucency/PERPIXEL_TRANSLUCENT)
           (AWTUtilities/isTranslucencyCapable (.getGraphicsConfiguration win)))
    (doto win
      (AWTUtilities/setWindowOpacity (float opacity))
      (AWTUtilities/setWindowOpaque true))
      ;(AWTUtilities/setWindowShape (RoundRectangle2D$Double. 0 0 (.getWidth win) (.getHeight win) 20 20))
    win))

(defn make-visible [win]
  (make-win-translucent win 0)
  (doto win
    (.setVisible true)
    (.toFront))
  (doseq [opacity (take 16 (iterate (partial + 0.05) 0.1))]
    (Thread/sleep 20) ;could speed up rather than linear?
    (make-win-translucent win opacity)))

(defn create-initial-win
  "Create a default window of default size."
  []
  (doto (JDialog.)
    (.setUndecorated true)
    (.setAlwaysOnTop true)
    (.. getContentPane (setBackground (Color. 0 81 115)))
    (.setSize MAX-WIN-WIDTH MAX-WIN-HEIGHT)))

(defn create-win-scrollpane [view]
  (doto (JScrollPane. view)
    (.setBorder (BorderFactory/createEmptyBorder))
    (.setOpaque false)
    (.. getViewport (setOpaque false))))

(defn create-window-with-labels
  "Create a default window, add all the labels, resize, center, add
  listeners and display."
  [labels]
  (let [win (create-initial-win)
        box (Box/createVerticalBox)
        scroll (create-win-scrollpane box)
        lbl-count (count labels)]
    (add-win-listeners win scroll)
    (.add win scroll)
    (.add box (Box/createVerticalGlue))
    (doseq [lbl labels]
      (.add box lbl)
      (if (> lbl-count 1) (.add box (Box/createHorizontalStrut (/ WIN-MARGIN 2))))
      (.add box (Box/createVerticalGlue)))
    (.validate win)
    (size-window win box)
    (center-window win)
    (make-visible win)))

(defn create-window-with-image
  "Create a default window, add an image label, resize, center, add
  listeners and display."
  [file-path]
  (let [win (create-initial-win)]
    (add-win-listeners win)
    (.add win (create-image-label file-path))
    (.validate win)
    (size-window win win) ;specify the win as the layout-manager
    (center-window win)
    (make-visible win)))

;public interface
(defn create-window [message]
  "Create the window displaying the message."
  (if (is-image? message)
    (-> message create-window-with-image)
    (-> message expand-tabs lines create-labels create-window-with-labels)))

;integration tests
(defn integration-tests []
  ;doesn't realise full width;
  (create-window "Go for a coffee.")
  ;honours line breaks, left aligns and handles tabs:
  (create-window "for(int i=0; i<10; i++){\n\ti-=1;\n\tg++;\n\tg++;\n\tg++;\n\tg++;\n\tg++;\n}")
  ;breaks and center justifies:
  (create-window "This is a really long sentence that should wrap and display in a smaller font. This is a really long sentence that should wrap and display in a smaller font.")
  ;realises full width but doesn't break:
  (create-window "07590 719599")
  )

(integration-tests)
