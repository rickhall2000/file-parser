(ns file-parser.person-test
  (:require [clojure.test :refer :all]
            [file-parser.person :refer :all]
            [file-parser.examples :refer [+sample-records+]]
            [java-time :as time]))

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

(deftest format-for-printing-test
  (testing "passing in a seq of maps returns a seq of vectors of strings"
    (is (= [["Wambach" "Abby" "Female" "Blue" "6/2/1980"]
            ["Pogba" "Paul" "Male" "Red" "3/15/1993"]
            ["Martinez" "Josef" "Male" "Peach" "5/19/1993"]])
        (format-for-printing +sample-records+))))