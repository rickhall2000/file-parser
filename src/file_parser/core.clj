(ns file-parser.core
  (:require [file-parser.sorter :refer [sort-by-keys]]
            [file-parser.person :as person]
            [file-parser.data :as db]
            [file-parser.api :as api]))

;; Todo: Handle error paths

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
    (let [data-load-result (db/make-data (first args))]
      (if (= (type data-load-result) java.lang.String)
        (println "Loading data failed: " data-load-result)
        (do
          (produce-output)
          (api/start))))
    (println "Please pass a single argument, containing the filename you wish to load")))
