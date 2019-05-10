(ns file-parser.sorter)

(defn at-bottom-level?
  [data]
  (or (map? (first data))
      (empty? (first data))))

(defn reassemble-data
  [data]
  (if (at-bottom-level? data)
    data
    (recur (apply concat data))))

(defn sort-asc-or-desc
  [key data]
  (let [sort-fn (if (vector? key)
                  (first key)
                  key)
        reverse-fn (if (and
                         (vector? key)
                         (= :desc (first (rest key))))
                     reverse
                     identity)]
    (->> data
         (sort-by sort-fn)
         (reverse-fn)
         (partition-by sort-fn))))

(defn- sort*
  [data remaining-keys]
  (cond
    (= 0 (count remaining-keys))
    data
    (not (at-bottom-level? data))
    (map #(sort* % remaining-keys) data)
    :else
    (recur
      (sort-asc-or-desc (first remaining-keys) data) (rest remaining-keys))))

(defn sort-by-keys
  [data sort-keys]
  (let [sort-keys (take 5 sort-keys)]                        ; only supporting sorting to 5 levels for safety
    (reassemble-data (sort* data sort-keys))))
