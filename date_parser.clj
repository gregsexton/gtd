(ns date-parser
  (:import
     (java.util Calendar GregorianCalendar)))

(defn parse-date [date-str]
  (let [cal (GregorianCalendar.)]
    (.add cal Calendar/MINUTE 1)
    (.getTime cal)))
