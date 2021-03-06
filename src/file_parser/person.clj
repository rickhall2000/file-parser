(ns file-parser.person
  (:require [java-time :as time]
            [clojure.string :as str]))

(def +output-fields+ [:LastName :FirstName :Gender :FavoriteColor :DateOfBirth])

(defrecord Person [LastName FirstName Gender FavoriteColor DateOfBirth])

(defn dob-string->date
  "Translate DateOfBirth to a date type"
  [row]
  (try
    (if (:DateOfBirth row)
      (update row :DateOfBirth (fn [s]
                                 (when s (time/local-date "M/d/yyyy" s))))
      row)
    (catch Throwable t
      "Error parsing date")))

(defn dob-date->string
  [row]
  (if (:DateOfBirth row)
    (update row :DateOfBirth (fn [d] (time/format "M/d/yyyy" d)))
    row))

(defn strings->Person
  [strings]
  {:pre [(= 5 (count strings))]}
  (try
    (let [person-map (->> strings
                          (zipmap +output-fields+)
                          (dob-string->date))]
      (if (= java.lang.String (type person-map))
        person-map
        (map->Person person-map)))
    (catch Throwable t "Error creating person from strings")))

(defn map-of-strings->Person
  [map-of-strings]
  (-> map-of-strings
      (dob-string->date)
      (map->Person)))

(defn Person->map-of-strings
  [person]
  (-> person
      (select-keys +output-fields+)
      (dob-date->string)))

(defn format-for-printing
  [data]
  (let [header (map name +output-fields+)]
    (->> data
         (map dob-date->string)
         (map (apply juxt +output-fields+))
         (reduce conj [header])
         (map #(str/join "\t" %)))))

(defn Person->json
  [person]
  (str
    (->> person
         (Person->map-of-strings)
         (reduce-kv
           (fn [m k v] (str m (pr-str (name k)) ":" (pr-str v)))
           "{"))
    "}"))
