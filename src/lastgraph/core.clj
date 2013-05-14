(ns lastgraph.core
  (:gen-class)
  (:require [me.raynes.least :as last.fm]
            [clojure.edn :as edn]
            [clojure.string :as s]
            [clj-time.format :as date]))

(defn gather [user k]
  (let [req (fn [opts] (last.fm/read "user.getRecentTracks" k
                                    (into {:user user
                                           :limit 50}
                                          opts)))
        pages (-> (req {:limit 1}) :recenttracks :attr :totalPages edn/read-string)]
    (mapcat (fn [page]
              (filter second
                      (map (juxt (comp :text :artist)
                                 (comp :text :date))
                           (-> {:page page} req :recenttracks :track))))
            (range 1 (inc pages)))))

(defn arrange [s]
  (apply merge-with (partial merge-with +)
         (map (fn [[artist date]]
                {(date/parse (date/formatter "dd MMM yyyy")
                             (first (s/split date #","))) {artist 1}}) s)))

(defn plot! [data] data)

(defn -main [user key & _]
  (-> user (gather key) arrange plot!))
