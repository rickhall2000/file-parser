(ns file-parser.person-test
  (:require [clojure.test :refer :all]
            [file-parser.person :refer :all]
            [file-parser.examples :refer [+sample-records+ +example-person-string-map+]]
            [java-time :as time]
            [clojure.data.json :as json])
  (:import (file_parser.person Person)))

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

(deftest strings->Person-test
  (testing "strings->Person should yield a person object with appropriate values"
    (let [example-strings ((apply juxt +output-fields+) +example-person-string-map+)
          test-person (strings->Person example-strings)]
      (is (= (map-of-strings->Person +example-person-string-map+)
             test-person))
      (is (= Person (type test-person)))
      (is (= java.time.LocalDate (type (:DateOfBirth test-person))))
      (is (= (:LastName +example-person-string-map+) (:LastName test-person))))))

(deftest map-of-strings->Person-test
  (testing "map-of-strings->Person yields a person record with appropriate values"
    (let [test-person (map-of-strings->Person +example-person-string-map+)]
      (is (= Person (type test-person)))
      (is (= java.time.LocalDate (type (:DateOfBirth test-person))))
      (is (= (:FirstName +example-person-string-map+) (:FirstName test-person))))))

(deftest Person->map-of-strings-test
  (testing "Person->map-of-strings is a dual of map-of-strings->Person"
    (let [test-person (map-of-strings->Person +example-person-string-map+)]
      (is (= +example-person-string-map+ (Person->map-of-strings test-person))))))

(deftest format-for-printing-test
  (testing "passing in a seq of maps returns a seq of vectors of strings"
    (is (= [["Wambach" "Abby" "Female" "Blue" "6/2/1980"]
            ["Pogba" "Paul" "Male" "Red" "3/15/1993"]
            ["Martinez" "Josef" "Male" "Peach" "5/19/1993"]])
        (format-for-printing +sample-records+))))

(deftest Person->json-test
  (testing "Person->json evaluates to equivalent json as json/write-str"
    (let [example (->Person "Hall" "Rick" "Male" "Blue" (time/local-date "M/d/yyyy" "1/10/1973"))]
      (is (= (json/read-str (json/write-str (Person->map-of-strings example)))
             (json/read-str (Person->json example)))))))
