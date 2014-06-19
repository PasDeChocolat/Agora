(ns agora.db.conn
  (:require
   [datomic.api :as d]))

(def db-uri "datomic:free://localhost:4334/agora")

(defn initialize-db
  "Create a blank Datomic DB"
  [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)
        schema (load-file "resources/datomic/agora_schema.edn")]
    (d/transact conn schema)
    conn))

(def conn
  (try
    (d/connect db-uri)
    (catch Exception e
      (println "Initializing Datomic!")
      (initialize-db db-uri))))

(defn real
  [lazy-entity]
  (d/touch (d/entity (d/db conn) lazy-entity)))
