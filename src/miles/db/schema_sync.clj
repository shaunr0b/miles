(ns genome.utils.db.schema-sync
  (:require [datomic.api :as d]
            [taoensso.timbre :refer (info)]))

(defn cardinality
  "Returns the cardinality (:db.cardinality/one or
   :db.cardinality/many) of the attribute"
  [db attr]
  (->>
   (d/q '[:find ?v
          :in $ ?attr
          :where
          [?attr :db/cardinality ?card]
          [?card :db/ident ?v]]
        db attr)
   ffirst))

(defn has-attribute?
  "Does database have an attribute named attr-name?"
  [db attr-name]
  (-> (d/entity db attr-name)
      :db.install/_attribute
      boolean))

(defn has-schema?
  "Does database have a schema named schema-name installed?
   Uses schema-attr (an attribute of transactions!) to track
   which schema names are installed."
  [db schema-attr schema-name]
  (and (has-attribute? db schema-attr)
       (-> (d/q '[:find ?e
                  :in $ ?sa ?sn
                  :where [?e ?sa ?sn]]
                db schema-attr schema-name)
           seq boolean)))

(defn- ensure-schema-attribute
  "Ensure that schema-attr, a keyword-valued attribute used
   as a value on transactions to track named schemas, is
   installed in database."
  [conn schema-attr]
  (when-not (has-attribute? (d/db conn) schema-attr)
      (d/transact conn [{:db/id (d/tempid :db.part/db)
                         :db/ident schema-attr
                         :db/valueType :db.type/keyword
                         :db/cardinality :db.cardinality/one
                         :db/doc "Name of schema installed by this transaction"
                         :db/index true
                         :db.install/_attribute :db.part/db}])))

(defn ensure-schemas
  "Ensure schemas are installed.
      schema-attr   a keyword valued attribute where transactions
                    are recorded by name
      schema-name   a keyword valued name of this transaction
      schema-map    a map from schema names to schema installation
                    maps. A schema installation map contains two
                    keys: :txes is the data to install, and :requires
                    is a list of other schema names that must also
                    be installed"
  [conn schema-attr schema-name txes]
  (ensure-schema-attribute conn schema-attr)
  (when-not (has-schema? (d/db conn) schema-attr schema-name)
    (println "Ensuring schema " schema-name)
    (doseq [tx txes]
      (try
         @(d/transact conn (cons {:db/id (d/tempid :db.part/tx)
                              schema-attr schema-name}
                             tx))
      (catch Exception e
        (println e))))))

(defn files-in-directory [directory]
  (let [directory (clojure.java.io/file directory)]
     (rest (file-seq directory))))

(defn ensure-schema-files
  "Given schema files are installed. Use filename as schema key."
  [conn schema-attr files]
  (doseq [file files]
    (let [schema-kw (keyword (.getName file))
          txes (clojure.edn/read-string
                  {:readers *data-readers*}
                  (slurp (str file)))]
      (ensure-schemas conn schema-attr schema-kw txes))))


(defn gen-schema!
  "Generate a new schema file.
    directory:  a string path in which to generate schema
    tag:        a keyword tag to use in labeling the file

    ex:
     (new-schema-file! \"resources/schemas\" :some-schema)"
  [directory tag & data]
    (let [timestamp (.getTime  (new java.util.Date))
          file-path (str directory "/" timestamp "-" (name tag) ".edn")]
      (->>
        (or data [[[]]])
        (apply pr-str )
        (spit file-path))
      (info "Created " file-path)
      :ok))


(defn ensure-schema-files-in-directory
  "Given schema files are installed. Use filename as schema key."
  [conn schema-attr directory]
  (ensure-schema-files conn schema-attr (files-in-directory directory)))

