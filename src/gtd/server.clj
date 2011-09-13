(ns gtd.server
  (:use [gtd.window :only (create-window)])
  (:use [gtd.date-parser :only (parse-date)])
  (:use [clojure.string :only (trim join)])
  (:use [clojure.contrib.server-socket :only (create-server)])
  (:import
     (java.text SimpleDateFormat)
     (java.util Timer TimerTask)
     (java.net InetAddress)
     (java.io BufferedReader InputStreamReader PrintWriter)))

;intended exports:
  ;start-server

(def *tasks* (atom nil))

(defn format-date [date]
  (.format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") date))

(defn print-tasks []
  (doseq [[msg date] @*tasks*]
    (println (str (format-date date)
                  " -- \""
                  (apply str (take 47 (remove #(= \newline %) (trim msg))))
                  "\""))))

(defn list-tasks []
  (if (empty? @*tasks*)
    (println "")
    (do
      (print-tasks)
      (println "."))))

(defn add-task-to-list
  "Adds this task to the task list keeping it chronologically sorted."
  [msg date]
  (swap! *tasks* 
         (comp (partial sort-by second) conj)
         [msg date]))

(defn remove-first [coll pred]
  (let [[pre post] (split-with (complement pred) coll)]
    (concat pre (rest post))))

(defn remove-task-from-list
  "Remove first task from list with this message."
  [msg]
  (swap! *tasks* remove-first #(= (first %) msg)))

(defn create-window-timer-task [message]
  (proxy [TimerTask] []
    (run []
      (create-window message)
      (remove-task-from-list message))))

(defn create-window-timer [request]
  (when-let [msg (:content request)]
    (doto (Timer.)
      (.schedule (create-window-timer-task msg) (:date request)))
    (add-task-to-list msg (:date request))))

(defn get-date-str [line]
  (apply str (drop 5 line)))

(defn parse-multi-line [lines]
  ;TODO: just assumed it is a task
  (let [request {:date (parse-date (get-date-str (first lines)))
                 :content (join "\n" (rest lines))}]
    (create-window-timer request)
    (println "ACCEPTED")))

(defn handle [in out]
  ;TODO: unit test this
  (binding
    [*in* (BufferedReader. (InputStreamReader. in))
     *out* (PrintWriter. out)]
    (loop [acc []
           continue false]
      (try (let [line (read-line)]
             (if continue
               (cond
                 (= line ".") (parse-multi-line acc)
                 :else (recur (conj acc line) continue))
               (cond
                 (= line "LIST") (list-tasks)
                 (= (apply str (take 4 line)) "TASK") (recur (conj acc line) true))))
        (catch Exception e nil)))))

(defn start-server
  "Starts a gtd server instance. Takes a specific port argument or
  without uses the default port of 61212. Binds to the localhost
  interface only."
  ([]
   (start-server 61212))
  ([port]
   (try 
     (create-server port handle 0
                    (InetAddress/getByAddress (byte-array (map byte [127 0 0 1]))))
     (catch java.net.BindException e
       (println "Could not bind to port: " port)))))
