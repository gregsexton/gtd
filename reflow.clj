(ns reflow)

;intended exports:
  ;reflow

(defn reflow
  "Takes a string and a number of desired breaks and returns a seq of as
  close to equal length as possible. Breaks are inserted at word
  boundaries only."
  [input breaks]
  (let [cnt (count input)]
    (map #(apply str %) (partition (quot cnt breaks) input))))
