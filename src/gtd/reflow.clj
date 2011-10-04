(ns gtd.reflow
  (:use [clojure.string :only (split join)]))

(defn- last-rest
  "Returns a seq of pairs made up of splitting the coll in all possible
  places. The pair consists of the last element of the front half and
  the entire back half."
  [coll]
  (lazy-seq
    (if (empty? coll) []
      (conj (last-rest (rest coll))
            [(first coll) (rest coll)]))))

(defn- cum-last-rest
  "Like last-rest but accumulates the first item in the pair."
  [coll]
  (letfn [(help [acc [[x xs] & ys]]
            (lazy-seq
              (if (empty? ys) [[(+ acc x) xs]]
                (conj (help (+ acc x) ys)
                      [(+ acc x) xs]))))]
    (help 0 (last-rest coll))))

(defn- sum [coll]
  (reduce + 0 coll))

(defn- min-item [f coll]
  (let [fs (map f coll)
        min-fs (apply min fs)
        pairs (map #(hash-map :f %1 :val %2) fs coll)]
    (:val (first (drop-while #(not= (:f %) min-fs)
                             pairs)))))

;TODO: is there a more idiomatic way of declaring this memoization?
(declare parts-mem)
(defn- parts
  "Splits coll into n summations of linear partitions maintaining a
  minimal difference across the partitoins."
  [coll n]
  (cond
    (= n 1) (list (sum coll))
    (empty? coll) (list 0)
    :else (min-item (partial apply max)
                    (map #(conj (parts-mem (second %) (dec n))
                                (first %))
                         (cum-last-rest coll)))))
(def parts-mem (memoize parts))

(defn- front-sum-count
  "How many values from the front of coll are required to sum to x."
  [coll x]
  (loop [coll coll
         acc 0 cnt 0]
    (cond
      (>= acc x) cnt
      (empty? coll) cnt
      :else (recur (rest coll) (+ acc (first coll)) (inc cnt)))))

(defn- takes
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

(defn- even-partitions 
  "Breaks coll into n partitions minimising the difference in score as
  calculated by applying f to each element in coll."
  [coll f n]
  (let [fs (map f coll)
        min-part (parts fs n)]
    (takes coll fs min-part)))

(defn- words [str-input]
  (split str-input #"\s+"))

(defn reflow
  "Takes a string and a number of desired breaks and returns a seq of as
  close to equal length as possible. Breaks are inserted at word
  boundaries only."
  [input breaks]
  (let [parts (even-partitions (words input) count breaks)]
    (map (partial join " ") parts)))
