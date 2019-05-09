(ns file-parser.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def s "sample.csv")

(defn read-lines
  [filename]
  (let [x (slurp filename)
        lines (str/split-lines x)]
    lines))

(defn find-delimiter
  [sample-string]
  (cond
    (str/includes? sample-string "|") "|"
    (str/includes? sample-string ",") ","
    (str/includes? sample-string " ") " "))

(defn csv-file->map
  [filename]
  (let [[header & rows] (read-lines filename)
        delimiter (find-delimiter (first rows))]
    rows))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (csv-file->map (first args))
    (println "Please pass a single argument, containing the filename you wish to load")))
