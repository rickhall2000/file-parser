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
  (with-redefs [file-parser.core/csv-file->map
                (constantly true)]
    (testing "The main function calls csv-file->map when it has 1 argument"
      (is (true? (-main "test"))))
    (testing "The main function does not call csv-file->map when it has no arguments"
      (is (nil? (-main))))
    (testing "The main function does not call csv-file->map when it has more than 1 argument"
      (is (nil? (-main "one" "two" "three"))))))

(deftest read-lines-splits-at-linebreaks
  (with-redefs [slurp (constantly "This is a
                                    Long text string
                                    That is on 3 lines.")]
    (testing "readlines function returns a list of lines"
      (is (= 3 (count (read-lines "hypothetical.file")))))))

(deftest find-delimiter-test
  (testing "find-delimiter can find a pipe"
    (is (= "|" (.toString (find-delimiter "This|Is|My|Test|String")))))
  (testing "find-delimiter can find a comma"
    (is (= "," (.toString (find-delimiter "This,Is,My,Test,String")))))
  (testing "find-delimiter can find a space"
    (is (= "\\s" (.toString (find-delimiter "This Is My Test String")))))
  (testing "find-delimiter doesn't find what isn't there"
    (is (nil? (find-delimiter "ThisIsABadlyFormattedString")))))

(deftest delimited-strings->map-test
  (testing
    "delimited-strings->map returns a sequence of maps with the header fields as keys"
    (is (= [{:A "2", :B "4"} {:A "5", :B "6"}]
           (delimited-strings->map ["A,B" "2,4" "5,6"])))))

(deftest format-for-printing-test
  (testing "passing in a seq of maps returns a seq of vectors of strings"
    (is (= [["Wambach" "Abby" "Female" "Blue" "6/2/1980"]
            ["Pogba" "Paul" "Male" "Red" "3/15/1993"]
            ["Martinez" "Josef" "Male" "Peach" "5/19/1993"]])
        (format-for-printing +sample-records+))))