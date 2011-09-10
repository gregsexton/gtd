(ns date-parser-tests
  (:use date-parser)
  (:use clojure.contrib.test-is)
  (import (java.util Calendar GregorianCalendar)))

;TODO: test case insensitivity
;TODO: parse error handling -- shouldn't explode!

(deftest absolute-absvals-return-true
  (is (true? (absolute? "2011-04-30")))
  (is (true? (absolute? "2011-04-30 14:32")))
  (is (true? (absolute? "2011-04-30 14:32:17")))
  (is (true? (absolute? "2011-04-30 02:32pm")))
  (is (true? (absolute? "2011-04-30 02:32 pm")))
  (is (true? (absolute? "2011-04-30 2:32 pm")))
  (is (true? (absolute? "2011-04-30 02:32:17pm")))
  (is (true? (absolute? "2011-04-30 02:32:17 pm")))
  (is (true? (absolute? "2011-04-30 2:32:17 pm")))
  (is (true? (absolute? "14:32")))
  (is (true? (absolute? "14:32:17")))
  (is (true? (absolute? "02:32pm")))
  (is (true? (absolute? "02:32 pm")))
  (is (true? (absolute? "2:32 pm")))
  (is (true? (absolute? "02:32:17pm")))
  (is (true? (absolute? "02:32:17 pm")))
  (is (true? (absolute? "2:32:17 pm"))))

(deftest absolute-relvals-return-false
  (is (false? (absolute? "now")))
  (is (false? (absolute? "-")))
  (is (false? (absolute? ".")))
  (is (false? (absolute? "")))
  (is (false? (absolute? "tomorrow")))
  (is (false? (absolute? "tomoro")))
  (is (false? (absolute? "1day")))
  (is (false? (absolute? "1 day")))
  (is (false? (absolute? "day")))
  (is (false? (absolute? "a day")))
  (is (false? (absolute? "1days")))
  (is (false? (absolute? "days1")))
  (is (false? (absolute? "3day")))
  (is (false? (absolute? "3days")))
  (is (false? (absolute? "day3")))
  (is (false? (absolute? "monday")))
  (is (false? (absolute? "next monday")))
  (is (false? (absolute? "7days")))
  (is (false? (absolute? "week")))
  (is (false? (absolute? "a week")))
  (is (false? (absolute? "next week")))
  (is (false? (absolute? "3week")))
  (is (false? (absolute? "3weeks")))
  (is (false? (absolute? "month")))
  (is (false? (absolute? "a month")))
  (is (false? (absolute? "next month")))
  (is (false? (absolute? "jan")))
  (is (false? (absolute? "january")))
  (is (false? (absolute? "year")))
  (is (false? (absolute? "a year")))
  (is (false? (absolute? "next year")))
  (is (false? (absolute? "2012")))
  (is (false? (absolute? "3seconds")))
  (is (false? (absolute? "3second")))
  (is (false? (absolute? "3secs")))
  (is (false? (absolute? "3sec")))
  (is (false? (absolute? "a second")))
  (is (false? (absolute? "second")))
  (is (false? (absolute? "sec")))
  (is (false? (absolute? "3minutes")))
  (is (false? (absolute? "3minute")))
  (is (false? (absolute? "3mins")))
  (is (false? (absolute? "3min")))
  (is (false? (absolute? "a minute")))
  (is (false? (absolute? "minute")))
  (is (false? (absolute? "min")))
  (is (false? (absolute? "3hours")))
  (is (false? (absolute? "3hour")))
  (is (false? (absolute? "3hr")))
  (is (false? (absolute? "3hrs")))
  (is (false? (absolute? "a hour")))
  (is (false? (absolute? "an hour")))
  (is (false? (absolute? "hour")))
  (is (false? (absolute? "hr"))))

(deftest get-date-absvals-return-correct-date
  (is (= (create-date 2011 3 30 14 32 0) (get-date "2011-04-30 14:32")))
  (is (= (create-date 2011 3 30 14 32 17) (get-date "2011-04-30 14:32:17")))
  (is (= (create-date 2011 3 30 14 32 0) (get-date "2011-04-30 02:32pm")))
  (is (= (create-date 2011 3 30 14 32 0) (get-date "2011-04-30 02:32 pm")))
  (is (= (create-date 2011 3 30 14 32 0) (get-date "2011-04-30 2:32 pm")))
  (is (= (create-date 2011 3 30 2 32 0) (get-date "2011-04-30 2:32 am")))
  (is (= (create-date 2011 3 30 14 32 17) (get-date "2011-04-30 02:32:17pm")))
  (is (= (create-date 2011 3 30 14 32 17) (get-date "2011-04-30 02:32:17 pm")))
  (is (= (create-date 2011 3 30 14 32 17) (get-date "2011-04-30 2:32:17 pm")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 0) (get-date "14:32")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 17) (get-date "14:32:17")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 0) (get-date "02:32pm")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 0) (get-date "02:32 pm")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 0) (get-date "2:32 pm")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 17) (get-date "02:32:17pm")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 17) (get-date "02:32:17 pm")))
  (is (= (create-date (get-year) (get-month) (get-day) 14 32 17) (get-date "2:32:17 pm"))))

(deftest get-offset-unit-relvals-return-correct-unit
  ;(is (= Calendar/SECOND (:unit (get-offset "now"))))
  ;(is (= Calendar/SECOND (:unit (get-offset "-"))))
  ;(is (= Calendar/SECOND (:unit (get-offset "."))))
  ;(is (= Calendar/SECOND (:unit (get-offset ""))))
  ;(is (= Calendar/DATE   (:unit (get-offset "tomorrow"))))
  ;(is (= Calendar/DATE   (:unit (get-offset "tomoro"))))
  (is (= Calendar/DATE   (:unit (get-offset "1day"))))
  (is (= Calendar/DATE   (:unit (get-offset "1 day"))))
  (is (= Calendar/DATE   (:unit (get-offset "day"))))
  (is (= Calendar/DATE   (:unit (get-offset "a day"))))
  (is (= Calendar/DATE   (:unit (get-offset "1days"))))
  (is (= Calendar/DATE   (:unit (get-offset "days1"))))
  (is (= Calendar/DATE   (:unit (get-offset "2day"))))
  (is (= Calendar/DATE   (:unit (get-offset "2days"))))
  (is (= Calendar/DATE   (:unit (get-offset "day2"))))
  ;(is (= Calendar/DATE   (:unit (get-offset "mon"))))
  ;(is (= Calendar/DATE   (:unit (get-offset "monday"))))
  ;(is (= Calendar/DATE   (:unit (get-offset "next monday"))))
  (is (= Calendar/DATE   (:unit (get-offset "7days"))))
  (is (= Calendar/DATE   (:unit (get-offset "week"))))
  (is (= Calendar/DATE   (:unit (get-offset "a week"))))
  (is (= Calendar/DATE   (:unit (get-offset "next week"))))
  (is (= Calendar/DATE   (:unit (get-offset "2week"))))
  (is (= Calendar/DATE   (:unit (get-offset "2weeks"))))
  (is (= Calendar/MONTH  (:unit (get-offset "month"))))
  (is (= Calendar/MONTH  (:unit (get-offset "a month"))))
  (is (= Calendar/MONTH  (:unit (get-offset "next month"))))
  ;(is (= Calendar/MONTH  (:unit (get-offset "jan"))))
  ;(is (= Calendar/MONTH  (:unit (get-offset "january"))))
  (is (= Calendar/YEAR   (:unit (get-offset "year"))))
  (is (= Calendar/YEAR   (:unit (get-offset "a year"))))
  (is (= Calendar/YEAR   (:unit (get-offset "next year"))))
  (is (= Calendar/SECOND (:unit (get-offset "2seconds"))))
  (is (= Calendar/SECOND (:unit (get-offset "2second"))))
  (is (= Calendar/SECOND (:unit (get-offset "2secs"))))
  (is (= Calendar/SECOND (:unit (get-offset "2sec"))))
  (is (= Calendar/SECOND (:unit (get-offset "a second"))))
  (is (= Calendar/SECOND (:unit (get-offset "second"))))
  (is (= Calendar/SECOND (:unit (get-offset "sec"))))
  (is (= Calendar/MINUTE (:unit (get-offset "2minutes"))))
  (is (= Calendar/MINUTE (:unit (get-offset "2minute"))))
  (is (= Calendar/MINUTE (:unit (get-offset "2mins"))))
  (is (= Calendar/MINUTE (:unit (get-offset "2min"))))
  (is (= Calendar/MINUTE (:unit (get-offset "a minute"))))
  (is (= Calendar/MINUTE (:unit (get-offset "minute"))))
  (is (= Calendar/MINUTE (:unit (get-offset "min"))))
  (is (= Calendar/HOUR   (:unit (get-offset "2hours"))))
  (is (= Calendar/HOUR   (:unit (get-offset "2hour"))))
  (is (= Calendar/HOUR   (:unit (get-offset "2hr"))))
  (is (= Calendar/HOUR   (:unit (get-offset "2hrs"))))
  (is (= Calendar/HOUR   (:unit (get-offset "a hour"))))
  (is (= Calendar/HOUR   (:unit (get-offset "an hour"))))
  (is (= Calendar/HOUR   (:unit (get-offset "hour"))))
  (is (= Calendar/HOUR   (:unit (get-offset "hr")))))

(deftest get-offset-value-relvals-return-correct-value
  ;(is (= 0 (:val (get-offset "now"))))
  ;(is (= 0 (:val (get-offset "-"))))
  ;(is (= 0 (:val (get-offset "."))))
  ;(is (= 0 (:val (get-offset ""))))
  ;(is (= 1 (:val (get-offset "tomorrow"))))
  ;(is (= 1 (:val (get-offset "tomoro"))))
  (is (= 1 (:val (get-offset "1day"))))
  (is (= 1 (:val (get-offset "1 day"))))
  (is (= 1 (:val (get-offset "day"))))
  (is (= 1 (:val (get-offset "a day"))))
  (is (= 1 (:val (get-offset "1days"))))
  (is (= 1 (:val (get-offset "days1"))))
  (is (= 2 (:val (get-offset "2day"))))
  (is (= 2 (:val (get-offset "2days"))))
  (is (= 2 (:val (get-offset "day2"))))
  ;(is (= (- 7 (- Calendar/MONDAY (get-day-of-week)))
         ;(:val (get-offset "monday"))))
  ;(is (= (- 7 (- Calendar/MONDAY (get-day-of-week)))
         ;(:val (get-offset "next monday"))))
  (is (= 7 (:val (get-offset "7days"))))
  (is (= 7 (:val (get-offset "week"))))
  (is (= 7 (:val (get-offset "a week"))))
  (is (= 7 (:val (get-offset "next week"))))
  (is (= 14 (:val (get-offset "2week"))))
  (is (= 14 (:val (get-offset "2weeks"))))
  (is (= 1 (:val (get-offset "month"))))
  (is (= 1 (:val (get-offset "a month"))))
  (is (= 1 (:val (get-offset "next month"))))
  ;(is (= (- 12 (- Calendar/JANUARY (get-month)))
         ;(:val (get-offset "jan"))))
  ;(is (= (- 12 (- Calendar/JANUARY (get-month)))
         ;(:val (get-offset "january"))))
  (is (= 1 (:val (get-offset "year"))))
  (is (= 1 (:val (get-offset "a year"))))
  (is (= 1 (:val (get-offset "next year"))))
  (is (= 2 (:val (get-offset "2seconds"))))
  (is (= 2 (:val (get-offset "2second"))))
  (is (= 2 (:val (get-offset "2secs"))))
  (is (= 2 (:val (get-offset "2sec"))))
  (is (= 1 (:val (get-offset "a second"))))
  (is (= 1 (:val (get-offset "second"))))
  (is (= 1 (:val (get-offset "sec"))))
  (is (= 2 (:val (get-offset "2minutes"))))
  (is (= 2 (:val (get-offset "2minute"))))
  (is (= 2 (:val (get-offset "2mins"))))
  (is (= 2 (:val (get-offset "2min"))))
  (is (= 1 (:val (get-offset "a minute"))))
  (is (= 1 (:val (get-offset "minute"))))
  (is (= 1 (:val (get-offset "min"))))
  (is (= 2 (:val (get-offset "2hours"))))
  (is (= 2 (:val (get-offset "2hour"))))
  (is (= 2 (:val (get-offset "2hr"))))
  (is (= 2 (:val (get-offset "2hrs"))))
  (is (= 1 (:val (get-offset "a hour"))))
  (is (= 1 (:val (get-offset "an hour"))))
  (is (= 1 (:val (get-offset "hour"))))
  (is (= 1 (:val (get-offset "hr")))))

(run-tests)
