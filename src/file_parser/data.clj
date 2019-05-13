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
   (try
     (-> (str/split line delimiter)
         (person/strings->Person))
     (catch Throwable t
       "Error processing line")))
  ([line]
    (line->Person line (find-delimiter line))))

(defn delimited-strings->Persons
  [[_header & rows]]
  (let [delimiter (find-delimiter (first rows))
        results (map #(line->Person % delimiter) rows)]
    (if (some #(= java.lang.String (type %)) results)
      "Error processing file"
      results)))

(defn csv-file->Persons
  [filename]
  (try
    (-> (read-lines filename)
        (delimited-strings->Persons))
    (catch java.io.FileNotFoundException e
      "File not found")
    (catch Throwable t
      (str "An error occurred" (type t)))))

(defn make-data
  [filename]
  (let [data (csv-file->Persons filename)]
    (if (= (type data) java.lang.String)
      data
      (reset! person-data data))))
