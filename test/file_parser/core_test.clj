(ns file-parser.core-test
  (:require [clojure.test :refer :all]
            [file-parser.core :refer :all]))

(deftest main-only-works-with-one-arg-test
  (with-redefs [file-parser.core/doer-function
                (constantly true)]
    (testing "The main function calls the doer when it has 1 argument"
      (is (true? (-main "test"))))
    (testing "The main function does not call the doer when it has no arguments"
      (is (nil? (-main))))
    (testing "The main function does not call the doer when it has more than 1 argument"
      (is (nil? (-main "one" "two" "three"))))))
