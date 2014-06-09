(ns agora.db.result
  (:require
   [agora.db.query :as query]))

(defn find-all-by
  "Returns all entities possessing attr (and optional val)."
  ([db attr]
     (query/qes '[:find ?e
            :in $ ?attr
            :where [?e ?attr]]
          db attr))
  ([db attr val]
     (query/qes '[:find ?e
            :in $ ?attr
            :where [?e ?attr ?val]]
          db attr val)))

(defn maybe
  "Returns the value of attr for e, or if-not if e does not possess
   any values for attr. Cardinality-many attributes will be
   returned as a set"
  ([db e attr]
     (maybe db e attr nil))
  ([db e attr if-not]
     (query/maybe db e attr if-not)))

(defn maybe-r
  "Returns the value of single result, or if-not"
  ([result]
     (maybe-r result nil))
  ([result if-not]
     (if (seq result)
       (query/only result)
       if-not)))
