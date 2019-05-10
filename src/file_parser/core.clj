(ns file-parser.core
  (:require [clojure.string :as str]
            [file-parser.sorter :refer [sort-by-keys]]
            [file-parser.person :as person])
  (:import [file_parser.person Person]))

(def s "sample.csv")


;; Todo: Handle error paths
;; Todo: Add Spec documentation

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
         (map (partial zipmap fields))
         (map person/map-of-strings->Person))))

(defn csv-file->map
  [filename]
  (-> (read-lines filename)
      (delimited-strings->map)))

(defn print-table
  [table]
  (doseq [line table]
    (println line)))

(defn produce-output
  [filename]
  (let [data (csv-file->map filename)
        sorts [[:Gender :LastName]
               [:DateOfBirth :LastName]
               [:LastName :desc]]]
    (doseq [sort sorts]
      (-> data
          (sort-by-keys sort)
          person/format-for-printing
          print-table))))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (produce-output (first args))
    (println "Please pass a single argument, containing the filename you wish to load")))
