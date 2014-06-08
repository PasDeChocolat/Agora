(ns agora.db.result
  (:require
   [agora.db.query :refer [only] :as query]))

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
       (only result)
       if-not)))

(defn maybe-rs
  "Returns the value of a cardinality many result, or if-not"
  ([result]
     (maybe-rs result nil))
  ([result if-not]
     (if (seq result)
       (into #{} (map first result))
       if-not)))
