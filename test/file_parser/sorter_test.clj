(ns file-parser.sorter-test
  (:require [clojure.test :refer :all]
            [file-parser.sorter :refer :all]
            [file-parser.examples :refer [+sample-records+]]))

(deftest at-bottom-level?-test
    (testing "At bottom level recognizes a vector of maps as reduced"
      (is (at-bottom-level? [{:a 1} {:a 2}])))
    (testing "At bottom level recognizes a vector of vector of maps as not reduced"
      (is (false? (at-bottom-level? [[{:a 1} {:b 2}]]))))
    (testing "At bottom level recognizes an empty vector as reduced"
      (is (at-bottom-level? []))))

(deftest reassemble-data-test
    (testing "Reassemble data does not change a seq that is already reduced"
      (let [sample-data [{:a 1} {:b 2}]]
        (is (= sample-data (reassemble-data sample-data)))))
    (testing "Reassemble data doesn't blow up with an empty vector"
      (is (= [] (reassemble-data []))))
    (testing "Reassemble data can flatten arbitrarily nested collections"
      (is (= [{:a 1} {:b 2} {:c 3}]
             (reassemble-data [[[{:a 1}]] [[{:b 2} {:c 3}]]])))))

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
             (map #(dissoc % :DateOfBirth) (sort-by-keys +sample-records+ [[:LastName :desc]]))))))



