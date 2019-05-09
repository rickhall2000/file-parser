(ns file-parser.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [java-time :as time]))

(def s "sample.csv")

(def +output-fields+ [:LastName :FirstName :Gender :FavoriteColor :DateOfBirth])

;; Todo: Handle error paths
;; Todo: Add Spec documentation
;; Todo: Make sort handle asc/desc better
;; Todo: Move sample data to test folder

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

(defn translate-dates
  "Translate DateOfBirth to a date type"
  [row]
  (update row :DateOfBirth (fn [s] (time/local-date "M/d/yyyy" s))))

(defn delimited-strings->map
  [[header & rows]]
  (let [delimiter (find-delimiter (first rows))
        fields (map keyword (str/split header delimiter))]
    (->> rows
         (map (fn [row] (str/split row delimiter)))
         (map (partial zipmap fields))
         (map translate-dates))))

(defn translate-date->string
  [row]
  (update row :DateOfBirth (fn [d] (time/format "M/d/yyyy" d))))

(defn format-for-printing
  [data]
  (let [header (map name +output-fields+)]
    (->> data
         (map translate-date->string)
         (map (apply juxt +output-fields+))
         (reduce conj [header])
         (map #(str/join "\t" %)))))

(defn sort-by-keys
  [data sort-key]
  (let [sort-fn (fn [x]
                  (vec ((apply juxt sort-key) x)))]
    (sort-by sort-fn data)))

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
  (let [data (csv-file->map filename)]
    (-> data
        (sort-by-keys [:Gender :LastName])
        format-for-printing
        print-table)
    (-> data
        (sort-by-keys [:DateOfBirth :LastName])
        format-for-printing
        print-table)
    (-> data
        (sort-by-keys [:LastName])
        reverse
        (format-for-printing)
        print-table)))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (produce-output (first args))
    (println "Please pass a single argument, containing the filename you wish to load")))
