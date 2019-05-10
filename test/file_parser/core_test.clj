(ns file-parser.core-test
  (:require [clojure.test :refer :all]
            [file-parser.core :refer :all]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [java-time :as time]
            [clojure.string :as str]))


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

(deftest dob-string->date-test
  (testing "dob-string->date converts :DateOfBirth to a date"
    (is (= java.time.LocalDate
           (type (:DateOfBirth (dob-string->date {:DateOfBirth "1/10/1973"}))))))
  (testing "dob-string doesn't change anything else"
    (let [test-map {:some-string "Some String"
                    :DateOfBirth "1/10/1973"}]
      (is (= (dissoc test-map :DateOfBirth)
             (dissoc (dob-string->date test-map) :DateOfBirth)))))
  (testing "dob-string->date doesn't blowup if DateOfBirth is missing"
    (is (= {:test "some-string"} (dob-string->date {:test "some-string"})))))

(deftest dob-date->string-test
  (testing "dob-date->string converts :DateOfBirth to a string"
    (is (= java.lang.String
           (type (:DateOfBirth (dob-date->string
                                 {:DateOfBirth (time/local-date "M/d/yyyy" "1/10/1973")}))))))
  (testing "dob-string doesn't change anything else"
    (let [test-map {:some-string "Some String"
                    :DateOfBirth (time/local-date "M/d/yyyy" "1/10/1973")}]
      (is (= (dissoc test-map :DateOfBirth)
             (dissoc (dob-date->string test-map) :DateOfBirth)))))
  (testing "dob-string->date doesn't blowup if DateOfBirth is missing"
    (is (= {:test "some-string"} (dob-date->string {:test "some-string"})))))

(deftest delimited-strings->map-test
  (testing
    "delimited-strings->map returns a sequence of maps with the header fields as keys"
    (is (= [{:LastName "Wambach", :FirstName "Abby", :Gender "Female", :FavoriteColor "Blue", :DateOfBirth (time/local-date 1980 6 2)}
            {:LastName "Pogba", :FirstName "Paul", :Gender "Male", :FavoriteColor "Red", :DateOfBirth (time/local-date 1993 3 15)}
            {:LastName "Martinez", :FirstName "Josef", :Gender "Male", :FavoriteColor "Peach", :DateOfBirth (time/local-date 1993 5 19)}]
           (delimited-strings->map
             ["LastName,FirstName,Gender,FavoriteColor,DateOfBirth"
              "Wambach,Abby,Female,Blue,6/2/1980"
              "Pogba,Paul,Male,Red,3/15/1993"
              "Martinez,Josef,Male,Peach,5/19/1993"])))))

(deftest format-for-printing-test
  (testing "passing in a seq of maps returns a seq of vectors of strings"
    (is (= [["Wambach" "Abby" "Female" "Blue" "6/2/1980"]
            ["Pogba" "Paul" "Male" "Red" "3/15/1993"]
            ["Martinez" "Josef" "Male" "Peach" "5/19/1993"]])
        (format-for-printing +sample-records+))))

(deftest sort-by-keys-test
  (testing "sort-by-keys can sort by Gender then LastName"
    (is (= '({:LastName "Wambach", :FirstName "Abby", :Gender "Female", :FavoriteColor "Blue"}
              {:LastName "Martinez", :FirstName "Josef", :Gender "Male", :FavoriteColor "Peach"}
              {:LastName "Pogba", :FirstName "Paul", :Gender "Male", :FavoriteColor "Red"})
           (map #(dissoc % :DateOfBirth) (sort-by-keys +sample-records+ [:Gender :LastName])))))
  (testing "sort-by-keys can sort by Date of Birth"
    (is (= '({:LastName "Wambach", :FirstName "Abby", :Gender "Female", :FavoriteColor "Blue"}
              {:LastName "Pogba", :FirstName "Paul", :Gender "Male", :FavoriteColor "Red"}
              {:LastName "Martinez", :FirstName "Josef", :Gender "Male", :FavoriteColor "Peach"})
           (map #(dissoc % :DateOfBirth) (sort-by-keys +sample-records+ [:DateOfBirth])))))
  (testing "sort-by-keys can sort by Date of Birth"
    (is (= '({:LastName "Wambach", :FirstName "Abby", :Gender "Female", :FavoriteColor "Blue"}
              {:LastName "Pogba", :FirstName "Paul", :Gender "Male", :FavoriteColor "Red"}
              {:LastName "Martinez", :FirstName "Josef", :Gender "Male", :FavoriteColor "Peach"})
           (reverse (map #(dissoc % :DateOfBirth) (sort-by-keys +sample-records+ [:LastName])))))))

(deftest main-only-works-with-one-arg-test
  (with-redefs [file-parser.core/produce-output
                (constantly true)]
    (testing "The main function calls csv-file->map when it has 1 argument"
      (is (true? (-main "test"))))
    (testing "The main function does not call csv-file->map when it has no arguments"
      (is (nil? (-main))))
    (testing "The main function does not call csv-file->map when it has more than 1 argument"
      (is (nil? (-main "one" "two" "three"))))))
