(ns sotesola2012.visualize
  (:use funnyqt.emf
        funnyqt.protocols
        funnyqt.query
        funnyqt.query.emf
        sotesola2012.metrics)
  (:require [sotesola2012.util :as util]))

(def make-id
  (memoize (fn [v] (gensym "v"))))

(defn classifier2node [jcls all-iclasses]
  (let [id (make-id jcls)]
    (str "  " id " [shape=record, label=\"{{"
         (when (has-type? jcls 'Interface)
           "«interface»\\n")
         (eget jcls :name) "}}\""
         "];\n")))

(defn target [tr]
  (the (reachables tr [p-seq [p-opt :classifierReferences] :target])))

(defn classifier-extends [jcls]
  (apply str
         (for [s (adjs jcls :extends)
               :let [t (target s)]
               :when (and t (util/internal-classifier? t))]
           (str "  " (make-id jcls) " -> " (make-id t)
                " [arrowhead=empty];\n"))))

(defn classifier-implements [jcls]
  (when (has-type? jcls 'Implementor)
    (apply str
           (for [s (adjs jcls :implements)
                 :let [t (target s)]
                 :when (util/internal-classifier? t)]
             (str "  " (make-id jcls) " -> " (make-id t)
                  " [arrowhead=empty, style=dashed];\n")))))

(defn used-classes [jcls]
  (reachables jcls
              [p-seq :members
               :typeReference
               [p-opt :classifierReferences]
               :target]))

(defn classifier-uses [icls jcls]
  (apply str
         (for [used (clojure.set/intersection
                     (disj icls jcls)
                     (used-classes jcls))]
           (str "  " (make-id jcls) " -> " (make-id used)
                " [arrowhead=open];\n"))))

(defn generate-dot [jm dotfile]
  (let [iclassifiers (util/internal-classifiers jm)
        nodes (apply str (mapv #(classifier2node % iclassifiers) iclassifiers))
        edges (str (apply str (mapv classifier-extends iclassifiers))
                   (apply str (mapv classifier-implements iclassifiers))
                   (apply str (mapv #(classifier-uses iclassifiers %) iclassifiers)))]
    (spit dotfile
          (str "digraph Extracted {\n"
               "  rankdir=BT;"
               "  ranksep=1.0;"
               nodes
               edges
               "}\n"))))

