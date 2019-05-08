(ns file-parser.core)

(defn doer-function
  [arg]
  (str "this is pointless " arg))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (doer-function (first args))
    (println "Please pass a single argument, containing the filename you wish to load")))
