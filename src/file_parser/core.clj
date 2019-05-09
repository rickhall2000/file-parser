(ns file-parser.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def s "sample.csv")

(defn read-lines
  [filename]
  (let [x (slurp filename)
        lines (str/split-lines x)]
    lines))

(defn csv-file->map
  [filename]
  (let [lines (read-lines filename)]
    lines))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (csv-file->map (first args))
    (println "Please pass a single argument, containing the filename you wish to load")))
