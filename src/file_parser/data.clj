(ns file-parser.data
  (:require [file-parser.sorter :refer [sort-by-keys]]
            [clojure.string :as str]
            [file-parser.person :as person]))

(def person-data (atom []))

(defn get-data
  ([sort]
   (let [data @person-data]
     (if sort
       (sort-by-keys data sort)
       data)))
  ([] (get-data nil)))

(defn add-person
  [new-person]
  (swap! person-data conj new-person))

(defn read-lines
  [filename]
  (let [x (slurp filename)
        lines (str/split-lines x)]
    lines))

(defn find-delimiter
  "Look for a supported delimiter char in a string and return a regex pattern"
  [sample-string]
  (cond
    (str/includes? sample-string "|") #"\|"
    (str/includes? sample-string ",") #","
    (str/includes? sample-string " ") #"\s"))

(defn line->Person
  ([line delimiter]
   (-> (str/split line delimiter)
       (person/strings->Person)))
  ([line]
    (line->Person line (find-delimiter line))))

(defn delimited-strings->map
  [[_header & rows]]
  (let [delimiter (find-delimiter (first rows))]
    (map #(line->Person % delimiter) rows)))

(defn csv-file->map
  [filename]
  (-> (read-lines filename)
      (delimited-strings->map)))

(defn make-data
  [filename]
  (reset! person-data (csv-file->map filename)))
