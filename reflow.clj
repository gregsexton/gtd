(ns reflow
  (:use [clojure.string :only (split join)]))

;intended exports:
  ;reflow

(defn splits [coll]
  (take (count coll) (map split-at (iterate inc 1) (repeat coll))))

(defn sum [coll]
  (reduce + 0 coll))

;TODO: use some sort of destructuring?
;TODO: is this going to be fast enough?? definitely not. 
(defn parts [coll n]
  (cond
    (= n 1) (list (list (sum coll)))
    (empty? coll) (list (repeat n 0))
    :else (mapcat #(map (partial cons (sum (first %)))
                        (parts (second %) (dec n)))
                  (splits coll))))

;TODO: destructuring
(defn min-item [f coll]
  (let [fs (map f coll)
        min-fs (apply min fs)
        pairs (map #(vector %1 %2) fs coll)]
    (second (first (drop-while #(not= (first %) min-fs)
                               pairs)))))

(defn part-diff [coll]
  (- (apply max coll)
     (apply min coll)))

(defn min-diff [colls]
  (min-item part-diff colls))

(defn front-sum-count
  "How many values from the front of coll are required to sum to x."
  [coll x]
  (loop [coll coll
         acc 0 cnt 0]
    (cond 
      (>= acc x) cnt
      (empty? coll) cnt
      :else (recur (rest coll) (+ acc (first coll)) (inc cnt)))))

(defn takes
  "Values should be a 1-to-1 mapping with coll. It is used to divide
  coll according to take-coll. Cumulative values are used."
  [coll values take-coll]
  (lazy-seq
    (if (empty? take-coll) '()
      (let [take-cnt (front-sum-count values (first take-coll))]
        (cons (take take-cnt coll)
              (takes (drop take-cnt coll)
                     (drop take-cnt values)
                     (rest take-coll)))))))

(defn even-partitions [coll f n]
  (let [fs (map f coll)
        min-part (min-diff (parts fs n))]
    (takes coll fs min-part)))

(defn words [str-input]
  (split str-input #"\s+"))

(defn reflow
  "Takes a string and a number of desired breaks and returns a seq of as
  close to equal length as possible. Breaks are inserted at word
  boundaries only."
  [input breaks]
  (let [parts (even-partitions (words input) count breaks)]
    (map (partial join " ") parts)))

;for testing only:

(def reflow-test '("This" "is" "a" "really" "long" "sentence" "that" "should" "wrap" "and" "display" "in" "a" "smaller" "font." "This" "is" "a" "really" "long" "sentence" "that" "should" "wrap" "and" "display" "in" "a" "smaller" "font."))

;(even-partitions reflow-test count 7) ;way slow
;(even-partitions reflow-test count 8) ;starting to disappear to never land
