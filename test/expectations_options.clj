(ns expectations-options
  (:require
   [agora.db.grid :refer [conn]]
   [datomic.api :as d]))

(defn create-empty-in-memory-db
  "Create a blank, in-memory Datomic DB"
  []
  (let [uri "datomic:mem://agora-test"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/datomic/agora_schema.edn")]
      (d/transact conn schema)
      conn)))

#_(defn before-run
  "before running suite"
  {:expectations-options :before-run}
  []
    (println "before suite"))

(defn in-context
  "rebind a var, expecations are run in the defined context"
  {:expectations-options :in-context}
  [work]
  (with-redefs [conn (create-empty-in-memory-db)]
    (work)))
