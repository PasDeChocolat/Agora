(ns agora.db.util
  (:require [datomic.api :as d]))

(defn list-attributes
  [db]
  (d/q '[:find ?attr-name
        :where
         [?attr :db/ident ?attr-name]
         [:db.part/db :db.install/attribute ?attr]]
      db))
