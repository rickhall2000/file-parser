(ns file-parser.api
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [file-parser.data :as db]
            [file-parser.person :as person]))

(defn ok
  [body & {:as headers}]
  {:status 200 :body body
   :headers (assoc headers "Content-Type" "application/json")})

(defn created
  [body & {:as headers}]
  {:status 201 :body body
   :headers headers})

(defn people->json
  [people]
  (str
    (->> people
         (map person/Person->json)
         (reduce str "["))
    "]"))

(def person-list
  {:name :person-list
   :enter
         (fn [context]
           (assoc context :response (ok (people->json (db/get-data)))))})

(def person-by-gender
  {:name :person-by-gender
   :enter
         (fn [context]
           (assoc context :response (ok (people->json (db/get-data [:Gender :LastName])))))})

(def person-by-birthdate
  {:name :person-by-birthdate
   :enter
         (fn [context]
           (assoc context :response (ok (people->json (db/get-data [:DateOfBirth])))))})

(def person-by-name
  {:name :person-by-name
   :enter
         (fn [context]
           (assoc context :response (ok (people->json (db/get-data [[:LastName :desc]])))))})

(def person-create
  {:name :person-create
   :enter (fn [context]
            (let [new-person (get-in context [:request :form-params :person])
                  person (db/line->Person new-person)]
              (db/add-person person)
              (assoc context :response (created "Person added"))))})

(def routes
  (route/expand-routes
    #{["/records" :post [(body-params/body-params) http/html-body person-create]]
      ["/records" :get person-list :route-name :person-list]
      ["/records/gender" :get person-by-gender :route-name :person-by-gender]
      ["/records/birthdate" :get person-by-birthdate :route-name :person-by-birthdate]
      ["/records/name" :get person-by-name :route-name :person-by-name]}))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8890})

(defn start []
  (http/start (http/create-server service-map)))
