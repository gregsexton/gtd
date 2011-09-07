(ns server
  (:use [window :only (create-window)])
  (:use [date-parser :only (parse-date)])
  (:use [clojure.string :only (join)]) 
  (:use [clojure.contrib.server-socket :only (create-server)])
  (:import
     (java.net InetAddress)
     (java.io BufferedReader InputStreamReader PrintWriter)))

;intended exports:
  ;start-server

(defn get-date-str [line]
  (apply str (drop 5 line)))

(defn parse-multi-line [lines]
  ;TODO: just assumed it is a task
  (let [request {:date (parse-date (get-date-str (first lines)))
                 :content (join "\n" (rest lines))}]
    ;TODO: launch off a timer here
    (create-window (:content request))
    (println "ACCEPTED")))

(defn handle [in out]
  ;TODO: unit test this
  (binding
    [*in* (BufferedReader. (InputStreamReader. in))
     *out* (PrintWriter. out)]
    (loop [acc []
           continue false]
      (let [line (read-line)]
        (if continue
          (cond
            (= line ".") (parse-multi-line acc)
            :else (recur (conj acc line) continue)) 
          (cond
            (= line "LIST") (println "Print tasks.")
            (= (apply str (take 4 line)) "TASK") (recur (conj acc line) true)))))))

(defn start-server
  "Starts a gtd server instance. Takes a specific port argument or
  without uses the default port of 61212. Binds to the localhost
  interface only."
  ([]
   (start-server 61212))
  ([port]
   (create-server port handle 0
                  (InetAddress/getByAddress (byte-array (map byte [127 0 0 1]))))))

(start-server)
