(ns date-parser
  (:use [clojure.string :only (trim)])
  (:use [clojure.contrib.cond])
  (:require [clojure.contrib.string :as s])
  (:import
     (java.util Calendar GregorianCalendar)))

;intended exports:
  ;parse-date

;date utilities
(defn create-date [year month date hour minute sec]
  (let [cal (GregorianCalendar.)]
    (.set cal year month date hour minute sec)
    (.set cal Calendar/MILLISECOND 0)
    (.getTime cal)))
(defn get-year []
  (.get (GregorianCalendar.) Calendar/YEAR))
(defn get-month []
  (.get (GregorianCalendar.) Calendar/MONTH))
(defn get-day []
  (.get (GregorianCalendar.) Calendar/DATE))
(defn get-day-of-week []
  (.get (GregorianCalendar.) Calendar/DAY_OF_WEEK))
(defn days-until [day]
  (defn help [day]
    (mod (+ (- day (get-day-of-week)) 7) 7))
  (let [mapping {:monday Calendar/MONDAY
                 :tuesday Calendar/TUESDAY
                 :wednesday Calendar/WEDNESDAY
                 :thursday Calendar/THURSDAY
                 :friday Calendar/FRIDAY
                 :saturday Calendar/SATURDAY
                 :sunday Calendar/SUNDAY}]
    (help (mapping day))))
(defn months-until [month]
  (defn help [month]
    (mod (+ (- month (get-month)) 12) 12))
  (let [mapping {:january Calendar/JANUARY
                 :february Calendar/FEBRUARY
                 :march Calendar/MARCH
                 :april Calendar/APRIL
                 :may Calendar/MAY
                 :june Calendar/JUNE
                 :july Calendar/JULY
                 :august Calendar/AUGUST
                 :september Calendar/SEPTEMBER
                 :october Calendar/OCTOBER
                 :november Calendar/NOVEMBER
                 :december Calendar/DECEMBER}]
    (help (mapping month))))

;utilities
(defn if-val
  "Returns expr if it evaluates to anything but nil otherwise default."
  [expr default]
  (if (nil? expr) default expr))

(defn first-of
  "Returns the first value that pred evaluates as true. If no value
  exists returns default if supplied otherwise nil."
  ([coll pred]
   (first-of coll pred nil))
  ([coll pred default]
   (let [candidates (drop-while (comp not pred) coll)]
     (if (empty? candidates)
       default
       (first candidates)))))

(defn absolute? [date-str]
  ((comp not empty? first)
     (re-find #"(\d{4}-\d{2}-\d{2} ?)?(\d{1,2}:\d{2}(:\d{2})?\s*(am|pm)?)?" date-str)))

;relative dates
(def units
  {:day    Calendar/DATE
   :week   Calendar/DATE
   :month  Calendar/MONTH
   :year   Calendar/YEAR
   :second Calendar/SECOND
   :minute Calendar/MINUTE
   :hour   Calendar/HOUR})
(def tokens
  {:day    [#"^days?" #"^d"]
   :week   [#"^weeks?" #"^w"]
   :month  [#"^months?"]
   :year   [#"^years?" #"^yr" #"^y"]
   :second [#"^seconds?" #"^secs?" #"^s"]
   :minute [#"^minutes?" #"^mins?"]
   :hour   [#"^hours?" #"^hrs?" #"^h"]})
(def separator #"^\s*")
(def num-regex #"^\d+")
(def one-regex #"^(?:(?:an?\s+)|(?:next\s+)|(?:1))")

(defn split-regex
  "Returns a vector of [match rest] where match is the string that
  matches the regex and rest is the remaining unmatched string. Assumes
  the regex starts with '^'. Returns nil if no match."
  [regex string]
  (if-let [match (re-find regex string)]
    [match (s/drop (count match) string)]))

(defn drop-regex [regex string]
  (if-let [[match tail] (split-regex regex string)]
    tail))

(defn drop-regexes [coll string]
  (first-of (map #(drop-regex % string) coll)
            (comp not nil?)))

(defn match-token
  "Returns map with keys [:token :rest] representing the matched token
  and the rest of the stream respectively. Returns nil if no match."
  [stream]
  (first-of
    (map #(if-let [rst (drop-regexes (% tokens) stream)]
                             {:token % :rest rst})
                          (keys tokens))
    (comp not nil?)))

(defn match-number
  "Returns map with keys [:number :rest] representing the matched number
  and the rest of the stream respectively. Returns nil if no match."
  [stream]
  ;TODO: can this be refactored and made a lot nicer? cond-let?
  (defn match-one [stream] ;treat one specially due to synonyms
    (if-let [[match tail] (split-regex one-regex stream)]
      {:number 1 :rest tail}))
  (if-let [match (match-one stream)] match
    (if-let [[match tail] (split-regex num-regex stream)]
      {:number (Integer. match) :rest tail})))

(defmacro special-form-cond-let
  "Takes a stream and a number of vectors for forms. Each vector should
  contain a regex, a number and a token value. If the regex matches the
  stream then a map is returned corresponding to the map expected of
  match-special."
  [stream & forms]
  (when-let [[[regex number token] & more] forms]
    `(if-let [tail# (drop-regex ~regex ~stream)]
       {:number ~number :token ~token :rest tail#}
       (special-form-cond-let ~stream ~@more))))

(defn match-special
  "Returns map with keys [:number :token :rest] representing the matched
  number and token pair of the special form. :rest is the remaining
  unmatched stream. Returns nil if no match."
  [stream]
  (special-form-cond-let stream
                         [#"^tomorrow" 1 :day]
                         [#"^now" 0      :second]
                         [#"^-" 0        :second]
                         [#"^\." 0       :second]
                         [#"^(?:mon(?:day)?)"      (days-until :monday)    :day]
                         [#"^(?:tues?(?:day)?)"    (days-until :tuesday)   :day]
                         [#"^(?:weds?(?:nesday)?)" (days-until :wednesday) :day]
                         [#"^(?:thur?(?:sday)?)"   (days-until :thursday)  :day]
                         [#"^(?:fri(?:day)?)"      (days-until :friday)    :day]
                         [#"^(?:sat(?:urday)?)"    (days-until :saturday)  :day]
                         [#"^(?:sun(?:day)?)"      (days-until :sunday)    :day]
                         [#"^(?:jan(?:uary)?)"   (months-until :january)   :month]
                         [#"^(?:feb(?:ruary)?)"  (months-until :february)  :month]
                         [#"^(?:mar(?:ch)?)"     (months-until :march)     :month]
                         [#"^(?:apr(?:il)?)"     (months-until :april)     :month]
                         [#"^(?:may)"            (months-until :may)       :month]
                         [#"^(?:june?)"          (months-until :june)      :month]
                         [#"^(?:july?)"          (months-until :july)      :month]
                         [#"^(?:aug(?:ust)?)"    (months-until :august)    :month]
                         [#"^(?:sep(?:tember)?)" (months-until :september) :month]
                         [#"^(?:oct(?:ober)?)"   (months-until :october)   :month]
                         [#"^(?:nov(?:ember)?)"  (months-until :november)  :month]
                         [#"^(?:dec(?:ember)?)"  (months-until :december)  :month]))

(defn tokenize [date-str]
  (lazy-seq
    (let [stream (drop-regex separator date-str)]
      (if (= date-str "") [:second 1]
        ;TODO: refactor using cond-let?
        ;try and match a token
        (if-let [match (match-token stream)]
          (conj (tokenize (:rest match))
                (:token match))
          ;try and match a number
          (if-let [match (match-number stream)]
            (conj (tokenize (:rest match))
                  (:number match))
            ;try and match a special form
            (if-let [match (match-special stream)]
              (conj (tokenize (:rest match))
                    (:token match)
                    (:number match)))))))))

(defn get-unit [tokens]
  (first-of tokens keyword? :second))

(defn get-time-value [tokens]
  (let [value (first-of tokens number? 0)]
    (if (= :week (get-unit tokens))
      (* value 7) value)))

(defn get-offset [date-str]
  (if (= date-str "") ;handle special special case
    {:unit Calendar/SECOND :val 0}
    (let [tokens (tokenize date-str)]
      {:unit (units (get-unit tokens))
       :val  (get-time-value tokens)})))

;absolute dates
(defn get-groups-date [date-str]
  (re-find #"(\d{4})-(\d{2})-(\d{2})" (if-val date-str "")))
(defn get-abs-year [date]
  (Integer. (nth (get-groups-date date) 1 (get-year))))
(defn get-abs-month [date]
  (dec (Integer. (nth (get-groups-date date) 2
                      (inc (get-month))))))
(defn get-abs-date [date]
  (Integer. (nth (get-groups-date date) 3 (get-day))))

(defn get-groups-time [time-str]
  (re-find #"(\d{1,2}):(\d{2})(:(\d{2}))?\s*(am|pm)?" time-str))
(defn pm? [date]
  (if-let [pm (nth (get-groups-time date) 5)]
    (= pm "pm")
    false))
(defn get-abs-hour [date]
  (let [hour (Integer. (nth (get-groups-time date) 1))]
    (if (and (pm? date) (< hour 13))
      (+ 12 hour) hour)))
(defn get-abs-min [date]
  (Integer. (nth (get-groups-time date) 2)))
(defn get-abs-sec [date]
  (Integer. (if-val (nth (get-groups-time date) 4)
                    "0")))

(defn get-date [date-str]
  (let [matches (re-find #"(\d{4}-\d{2}-\d{2} ?)?(\d{1,2}:\d{2}(:\d{2})?\s*(am|pm)?)?" date-str)]
    (create-date (get-abs-year  (nth matches 1))
                 (get-abs-month (nth matches 1))
                 (get-abs-date  (nth matches 1))
                 (get-abs-hour  (nth matches 2))
                 (get-abs-min   (nth matches 2))
                 (get-abs-sec   (nth matches 2)))))

(defn parse-date [date-str]
  (let [input (trim date-str)]
    (if (absolute? input)
      (get-date input)
      (let [cal (GregorianCalendar.)
            offset (get-offset input)]
        (.add cal (:unit offset) (:val offset))
        (.getTime cal)))))
