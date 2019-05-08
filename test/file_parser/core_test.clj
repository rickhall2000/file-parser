(ns file-parser.core-test
  (:require [clojure.test :refer :all]
            [file-parser.core :refer :all]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


(def +sample-records+
  [{:LastName "Wambach"
    :FirstName "Abby"
    :Gender "Female"
    :FavoriteColor "Blue"
    :DateOfBirth "6/2/1980"}
   {:LastName "Pogba"
    :FirstName "Paul"
    :Gender "Male"
    :FavoriteColor "Red"
    :DateOfBirth "3/15/1993"}
   {:LastName "Martinez"
    :FirstName "Josef"
    :Gender "Male"
    :FavoriteColor "Peach"
    :DateOfBirth "5/19/1993"}])

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

(deftest main-only-works-with-one-arg-test
  (with-redefs [file-parser.core/doer-function
                (constantly true)]
    (testing "The main function calls the doer when it has 1 argument"
      (is (true? (-main "test"))))
    (testing "The main function does not call the doer when it has no arguments"
      (is (nil? (-main))))
    (testing "The main function does not call the doer when it has more than 1 argument"
      (is (nil? (-main "one" "two" "three"))))))
