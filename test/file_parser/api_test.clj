(ns file-parser.api-test
  (:require [clojure.test :refer :all]
            [file-parser.api :as api]
            [file-parser.data :as db]
            [clojure.data.json :as json]
            [io.pedestal.test :as test]
            [io.pedestal.http :as http]))

(defonce server
         (http/start (http/create-server
                       (assoc api/service-map
                         ::http/join? false))))

(def +data-file+ "test/data/sample.csv")

(deftest person-by-name-test
  (testing "getting /records/name returns people ordered by last name descending in valid json"
    (db/make-data +data-file+)
    (is (= (map :LastName (db/get-data [[:LastName :desc]]))
           (map #(get % "LastName")
                (json/read-str
                  (:body
                    (test/response-for
                      (:io.pedestal.http/service-fn server)
                      :get "/records/name"))))))))

(deftest person-by-birthdate-test
  (testing "getting /records/birthdate returns people ordered by date of birth in valid json"
    (db/make-data +data-file+)
    (is (= (map :LastName (db/get-data [:DateOfBirth]))
           (map #(get % "LastName")
                (json/read-str
                  (:body
                    (test/response-for
                      (:io.pedestal.http/service-fn server)
                      :get "/records/birthdate"))))))))

(deftest person-by-gender-test
  (testing "getting /records/gend returns people ordered by gender then last name in valid json"
    (db/make-data +data-file+)
    (is (= (map :LastName (db/get-data [:Gender :LastName]))
           (map #(get % "LastName")
                (json/read-str
                  (:body
                    (test/response-for
                      (:io.pedestal.http/service-fn server)
                      :get "/records/gender"))))))))

(deftest create-person-test
  (testing "Posting to /records creates a person"
    (with-redefs [db/person-data (atom [])]
      (is (= 201 (:status (test/response-for
                            (:io.pedestal.http/service-fn server)
                            :post "/records"
                            :headers {"Content-Type" "application/x-www-form-urlencoded"}
                            :body "person=Hamm Mia Female Blue 3/17/1972"))))
      (is (= 1 (count @db/person-data)))))
  (testing "Posting invalid data returns a 400"
    (is (= 400 (:status (test/response-for
                          (:io.pedestal.http/service-fn server)
                          :post "/records"
                          :headers {"Content-Type" "application/x-www-form-urlencoded"}
                          :body "person=Han Solo, Captain of the Millennium Falcon"))))))

(deftest non-existext-route-test
  (testing "Hitting a route that doesn't exist returns a 404"
    (is (= 404
           (:status
             (test/response-for
               (:io.pedestal.http/service-fn server)
               :get "some/other/route"))))))