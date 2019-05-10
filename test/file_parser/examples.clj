(ns file-parser.examples
  (:require [java-time :as time]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            ))

(def +sample-records+
  [{:LastName "Wambach"
    :FirstName "Abby"
    :Gender "Female"
    :FavoriteColor "Blue"
    :DateOfBirth (time/local-date 1980 6 2)}
   {:LastName "Pogba"
    :FirstName "Paul"
    :Gender "Male"
    :FavoriteColor "Red"
    :DateOfBirth (time/local-date 1993 3 15)}
   {:LastName "Martinez"
    :FirstName "Josef"
    :Gender "Male"
    :FavoriteColor "Peach"
    :DateOfBirth (time/local-date 1993 5 19)}])

(defn map->rows
  [data]
  (let [header (-> data first keys)
        rows (map (apply juxt header) data)
        printable-header (map name header)]
    (reduce conj [printable-header] rows)))

(defn make-csv-file
  "This writes a csv file that may be used in dev and testing"
  ([filename data]
   (with-open [writer (io/writer filename)]
     (csv/write-csv writer (map->rows data))))
  ([data] (make-csv-file "sample.csv" data))
  ([] (make-csv-file +sample-records+)))
