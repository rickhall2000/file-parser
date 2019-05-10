(ns file-parser.core-test
  (:require [clojure.test :refer :all]
            [file-parser.core :refer :all]
            [java-time :as time]
            [file-parser.examples :refer [+sample-records+]]
            [file-parser.person :as person]))

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
    (is (= (map person/map->Person [{:LastName "Wambach", :FirstName "Abby", :Gender "Female", :FavoriteColor "Blue", :DateOfBirth (time/local-date 1980 6 2)}
                                    {:LastName "Pogba", :FirstName "Paul", :Gender "Male", :FavoriteColor "Red", :DateOfBirth (time/local-date 1993 3 15)}
                                    {:LastName "Martinez", :FirstName "Josef", :Gender "Male", :FavoriteColor "Peach", :DateOfBirth (time/local-date 1993 5 19)}])
           (delimited-strings->map
             ["LastName,FirstName,Gender,FavoriteColor,DateOfBirth"
              "Wambach,Abby,Female,Blue,6/2/1980"
              "Pogba,Paul,Male,Red,3/15/1993"
              "Martinez,Josef,Male,Peach,5/19/1993"])))))

(deftest main-only-works-with-one-arg-test
  (with-redefs [file-parser.core/produce-output
                (constantly true)]
    (testing "The main function calls csv-file->map when it has 1 argument"
      (is (true? (-main "test"))))
    (testing "The main function does not call csv-file->map when it has no arguments"
      (is (nil? (-main))))
    (testing "The main function does not call csv-file->map when it has more than 1 argument"
      (is (nil? (-main "one" "two" "three"))))))

