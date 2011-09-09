(ns date-parser
  (:use [clojure.string :only (trim)])
  (:import
     (java.util Calendar GregorianCalendar)))

;intended exports:
  ;parse-date

(defn if-val [expr default]
  (if (nil? expr) default expr))

(defn get-offset-unit [date-str]
  Calendar/MINUTE)

(defn get-offset-value [date-str]
  10)

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

(defn get-groups-date [date-str]
  (re-find #"(\d{4})-(\d{2})-(\d{2})" (if-val date-str "")))
(defn get-abs-year [date]
  (Integer. (nth (get-groups-date date) 1 (get-year))))
(defn get-abs-month [date]
  (dec (Integer. (nth (get-groups-date date)
                      2
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

(defn absolute? [date-str]
  ((comp not empty? first)
     (re-find #"(\d{4}-\d{2}-\d{2} ?)?(\d{1,2}:\d{2}(:\d{2})?\s*(am|pm)?)?" date-str)))

(defn parse-date [date-str]
  (let [input (trim date-str)]
    (if (absolute? input)
      (get-date input)
      (let [cal (GregorianCalendar.)]
        (.add cal (get-offset-unit input) (get-offset-value input))
        (.getTime cal)))))
