(ns file-parser.core-test
  (:require [clojure.test :refer :all]
            [file-parser.core :refer :all]
            [file-parser.examples :refer [+sample-records+]]))

(deftest main-only-works-with-one-arg-test
  (with-redefs [file-parser.core/produce-output (constantly true)
                file-parser.core/start (constantly true)
                file-parser.data/make-data (constantly true)]
    (testing "The main function calls csv-file->map when it has 1 argument"
      (is (true? (-main "test"))))
    (testing "The main function does not call csv-file->map when it has no arguments"
      (is (nil? (-main))))
    (testing "The main function does not call csv-file->map when it has more than 1 argument"
      (is (nil? (-main "one" "two" "three"))))))

