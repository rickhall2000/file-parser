(ns file-parser.data-test
  (:require [clojure.test :refer :all]
            [file-parser.data :refer :all]
            [file-parser.person :as person]
            [java-time :as time])
  (:import (file_parser.person Person)))

(deftest read-lines-splits-at-linebreaks
  (with-redefs [slurp (constantly "This is a
                                    Long text string
                                    That is on 3 lines.")]
    (testing "readlines function returns a list of lines"
      (is (= 3 (count (read-lines "hypothetical.file")))))))

(deftest find-delimiter-test
  (testing "find-delimiter can find a pipe"
    (is (= "\\|" (.toString (find-delimiter "This|Is|My|Test|String")))))
  (testing "find-delimiter can find a comma"
    (is (= "," (.toString (find-delimiter "This,Is,My,Test,String")))))
  (testing "find-delimiter can find a space"
    (is (= "\\s" (.toString (find-delimiter "This Is My Test String")))))
  (testing "find-delimiter doesn't find what isn't there"
    (is (nil? (find-delimiter "ThisIsABadlyFormattedString")))))

(deftest line->Person-test
  (testing "line->Person works as well as strings->Person for lines with commas"
    (is (= (line->Person "Rooney,Wayne,Male,Red,10/24/1985")
           (person/strings->Person ["Rooney" "Wayne" "Male" "Red" "10/24/1985"]))))
  (testing "line->Person works as well as strings->Person for lines with pipes"
    (is (= (line->Person "Rapinoe|Megan|Female|Blue|7/5/1985")
           (person/strings->Person ["Rapinoe" "Megan" "Female" "Blue" "7/5/1985"]))))
  (testing "line->Person works as well as strings->Person for lines with spaces"
    (is (= (line->Person "Cantona Eric Male Red 5/24/1966")
           (person/strings->Person ["Cantona" "Eric" "Male" "Red" "5/24/1966"]))))
  (testing "line->Person returns an error string for a line that doesn't conform"
    (is (= java.lang.String (type (line->Person "Four score and seven years ago"))))))

(deftest delimited-strings->Persons-test
  (testing "delimited-strings->Persons returns a sequence of Persons with the header fields as keys"
    (is (= (map person/map->Person [{:LastName "Wambach", :FirstName "Abby", :Gender "Female", :FavoriteColor "Blue", :DateOfBirth (time/local-date 1980 6 2)}
                                    {:LastName "Pogba", :FirstName "Paul", :Gender "Male", :FavoriteColor "Red", :DateOfBirth (time/local-date 1993 3 15)}
                                    {:LastName "Martinez", :FirstName "Josef", :Gender "Male", :FavoriteColor "Peach", :DateOfBirth (time/local-date 1993 5 19)}])
           (delimited-strings->Persons
             ["LastName,FirstName,Gender,FavoriteColor,DateOfBirth"
              "Wambach,Abby,Female,Blue,6/2/1980"
              "Pogba,Paul,Male,Red,3/15/1993"
              "Martinez,Josef,Male,Peach,5/19/1993"]))))
  (testing "delimited-strings->Persons returns Error processing file when it gets a bunch of gibberish"
    (is (= "Error processing file"
           (delimited-strings->Persons ["To be or not to be, that is the question:"
                                        "Whether it is nobler in the mind to suffer"
                                        "The slings and arrows of outrageous fortune,"
                                        "Or to take up arms against a sea of troubles"
                                        "And by opposing end them."])))))

(deftest csv-file->map-test
  (testing "csv-file->maps returns 3 maps on the included sample file"
    (let [sample-people (csv-file->Persons "test/data/sample.csv")]
      (is (= 3 (count sample-people)))
      (is (every? #(= (type %) Person) sample-people))))
  (testing "csv-file->maps returns File not found when the file does not exist"
    (is (= "File not found" (csv-file->Persons "baseball-players.txt")))))