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
  "Look for a supported delimiter char in a string and return a regex pattern"
  [sample-string]
  (cond
    (str/includes? sample-string "|") #"|"
    (str/includes? sample-string ",") #","
    (str/includes? sample-string " ") #"\s"))

(defn delimited-strings->map
  [[header & rows]]
  (let [delimiter (find-delimiter (first rows))
        fields (map keyword (str/split header delimiter))]
    (->> rows
         (map (fn [row] (str/split row delimiter)))
         (map (partial zipmap fields)))))

(defn csv-file->map
  [filename]
  (-> (read-lines filename)
      (delimited-strings->map)))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (csv-file->map (first args))
    (println "Please pass a single argument, containing the filename you wish to load")))
