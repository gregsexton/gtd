(ns gtd.core
  (:use [gtd.server :only (start-server)])
  (:gen-class))

(defn -main [& args]
  (if (empty? args)
    (start-server)
    (start-server (Integer. (first args)))))
