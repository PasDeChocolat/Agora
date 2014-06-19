(ns agora.db.fixtures
  (:require
   [agora.db.conn :refer [conn]]
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

(defn in-test-db-context
  "rebind a var, expecations are run in the defined context"
  [work]
  (with-redefs [conn (create-empty-in-memory-db)]
    (work)))

(defn clean-db
  [f]
  (create-empty-in-memory-db)
  (in-test-db-context f))
