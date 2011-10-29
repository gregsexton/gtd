(defproject gtd "0.2.0"
  :url "http://www.gregsexton.org/portfolio/gtd-get-things-displayed/"
  :description "gtd -- get things displayed -- is an easy way of
               displaying text in large on your primary screen. It uses
               a client/server model and can be used to display anything
               possible from the command line."
  :dev-dependencies [[lein-daemon "0.3.1"]]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :daemon {:gtd {:ns gtd.core 
                 :pidfile "gtd.pid"}}
  :main gtd.core)
