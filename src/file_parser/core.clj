(ns file-parser.core
  (:require [file-parser.sorter :refer [sort-by-keys]]
            [file-parser.person :as person]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [file-parser.data :as db]))

;; Todo: Handle error paths


(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))


(def person-list
  {:name :person-list
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (db/get-data)))))})

(def person-by-gender
  {:name :person-by-gender
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (db/get-data [:Gender :LastName])))))})

(def person-by-birthdate
  {:name :person-by-birthdate
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (db/get-data [:DateOfBirth])))))})

(def person-by-name
  {:name :person-by-name
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (db/get-data [[:LastName :desc]])))))})

(def person-create
  {:name :person-create
   :enter (fn [context]
            (let [new-person (get-in context [:request :form-params :person])
                  person (db/line->Person new-person)]
              (db/add-person person)
              (assoc context :response (created "cool"))))})

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

(defn print-table
  [table]
  (doseq [line table]
    (println line)))

(defn produce-output
  []
  (let [sorts [[:Gender :LastName]
               [:DateOfBirth :LastName]
               [:LastName :desc]]]
    (doseq [sort sorts]
      (-> sort
          db/get-data
          person/format-for-printing
          print-table))))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (do
      (db/make-data (first args))
      (produce-output)
      (start))
    (println "Please pass a single argument, containing the filename you wish to load")))
