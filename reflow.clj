(ns reflow)

;intended exports:
  ;reflow

;TODO: ensure to trim resulting strings after reflow
(defn reflow
  "Takes a string and a number of desired breaks and returns a seq of as
  close to equal length as possible. Breaks are inserted at word
  boundaries only."
  [input breaks]
  (let [cnt (count input)]
    (if (zero? (quot cnt breaks))
      (map #(apply str %) (partition 1 input))
      (map #(apply str %) (partition (quot cnt breaks) input)))))
