(ns file-parser.core
  (:require [clojure.string :as str]
            [file-parser.sorter :refer [sort-by-keys]]
            [file-parser.person :as person]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params])
  (:import [file_parser.person Person]))

;; Todo: Handle error paths

(def person-data (atom []))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))

(defn get-data
  ([sort]
   (let [data @person-data]
     (if sort
       (sort-by-keys data sort)
       data)))
  ([] (get-data nil)))

(def person-list
  {:name :person-list
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (get-data)))))})

(def person-by-gender
  {:name :person-by-gender
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (get-data [:Gender :LastName])))))})

(def person-by-birthdate
  {:name :person-by-birthdate
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (get-data [:DateOfBirth])))))})

(def person-by-name
  {:name :person-by-name
   :enter
         (fn [context]
           (assoc context :response (ok (map person/Person->json (get-data [[:LastName :desc]])))))})

(defn read-lines
  [filename]
  (let [x (slurp filename)
        lines (str/split-lines x)]
    lines))

(defn find-delimiter
  "Look for a supported delimiter char in a string and return a regex pattern"
  [sample-string]
  (cond
    (str/includes? sample-string "|") #"|"
    (str/includes? sample-string ",") #","
    (str/includes? sample-string " ") #"\s"))

(defn delimited-strings->map
  [[header & rows]]
  (let [delimiter (find-delimiter (first rows))
        fields (map keyword (str/split header delimiter))]
    (->> rows
         (map (fn [row] (str/split row delimiter)))
         (map (partial zipmap fields))
         (map person/map-of-strings->Person))))

(defn csv-file->map
  [filename]
  (-> (read-lines filename)
      (delimited-strings->map)))

(defn add-person
  [new-person]
  (swap! person-data conj new-person))

(def person-create
  {:name :person-create
   :enter (fn [context]
            (let [new-person (get-in context [:request :form-params :person])
                  delim (find-delimiter new-person)
                  fields (str/split new-person delim)
                  person (person/strings->Person fields)]
              (add-person person)
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
  (let [data @person-data
        sorts [[:Gender :LastName]
               [:DateOfBirth :LastName]
               [:LastName :desc]]]
    (doseq [sort sorts]
      (-> data
          (sort-by-keys sort)
          person/format-for-printing
          print-table))))

(defn make-data
  [filename]
  (reset! person-data (csv-file->map filename)))

(defn -main
  [& args]
  (if
    (= (count args) 1)
    (do
      (make-data (first args))
      (produce-output)
      (start))
    (println "Please pass a single argument, containing the filename you wish to load")))
