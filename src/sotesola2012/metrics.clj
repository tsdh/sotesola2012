(ns sotesola2012.metrics
  (:use funnyqt.emf
        funnyqt.query
        funnyqt.query.emf
        funnyqt.protocols)
  (:import [java.util.concurrent ForkJoinPool RecursiveTask]))

(def ^{:dynamic true
       :doc "A function that should return all classes for which the metrics
  should be calculated."}
  *get-classes-fn*
  (fn [m]
    (eallobjects m 'Class)))

(defn classifier-qname
  [c]
  (if-let [parent (econtainer c)]
    (if (has-type? parent 'Classifier)
      (str (classifier-qname parent) "$" (eget c :name))
      (clojure.string/replace (eget parent :name) #"\.java$" ""))
    (eget c :name)))

(def ^ForkJoinPool fj-pool (ForkJoinPool.))

(defn fj-do ^RecursiveTask [vs metric]
  (proxy [RecursiveTask] []
    (compute []
      (doall
       (if (< (count vs) 5)
         (map (fn [c]
                [(metric c) (classifier-qname c)])
              vs)
         (let [half (int (/ (count vs) 2))
               vs1 (subvec vs 0 half)
               fj1 (.fork (fj-do vs1 metric))
               vs2 (subvec vs half)
               r2 (.compute (fj-do vs2 metric))
               r1 (.join fj1)]
           (concat r1 r2)))))))

(defn apply-metric
  "Applies the given metric to all classes in parallel using a ForkJoinPool."
  [g metric]
  (sort
   (seq-compare #(- %2 %1) compare)
   (let [vs (vec (*get-classes-fn* g))]
     (.invoke fj-pool ^RecursiveTask (fj-do vs metric)))))


;;*** Depth of Inheritance Tree

(defn depth-of-inheritance-tree
  "Returns the depth of the inheritance tree of Type t.
  This only works for types whose source code has been parsed, but not for
  types that were merely referenced in some jar.  For example, the jamopp model
  does not contain the information that java.lang.Integer extends
  java.lang.Number, so that its DIT is actually 2, not 1."
  [t]
  (let [supers (reachables t [p-seq :extends [p-opt :classifierReferences] :target])
        ;; Because Object extends itself...
        supers (disj supers t)]
    (cond
     (seq supers) (inc (apply max (map depth-of-inheritance-tree supers)))
     (when-let [cu (econtainer t)]
       (= (eget cu :name) "java.lang.Object.java")) 0
     :else 1)))

(defn classes-by-depth-of-inheritance-tree
  [g]
  (apply-metric g depth-of-inheritance-tree))


;;*** Coupling between Objects

(defn coupled-classes
  "Given a Class `c', calculates all coupled classes."
  [c]
  (reachables c
    [p-seq :members
     ;; :statements for ClassMethods, :initialValue for Fields
     [p-alt :statements :initialValue]
     [p-* [<>--]]
     [p-restr '[java.references.MethodCall java.references.IdentifierReference]]
     ;; Matches both the ClassMethod that is the target of a MethodCall as well
     ;; as the Field that is the target of a IdentifierReference
     :target
     [p-restr '[java.members.ClassMethod java.members.Field]]
     --<>
     [p-restr 'java.classifiers.Class #(not (= c %1))]]))

(defn classes-by-coupling-between-objects
  [g]
  (apply-metric g #(count (coupled-classes %))))

;;*** Weighted Methods per Class

(defn cyclomatic-complexity
  "Returns the cyclomatic complexity of the given method."
  [m]
  (-> (reachables
       m [p-seq :statements
          [p-* <>--]
          [p-restr '[java.statements.NormalSwitchCase java.statements.DefaultSwitchCase
                     java.statements.Condition java.statements.ForLoop
                     java.statements.DoWhileLoop java.statements.WhileLoop]]])
      count
      inc))

(defn weighted-method-per-class
  "Returns the WMC metric for the given class c."
  [c]
  (reduce + (map cyclomatic-complexity
                 (reachables c [p-seq :members
                                      [p-restr 'java.members.Method]]))))

(defn classes-by-weighted-methods-per-class
  [g]
  (apply-metric g weighted-method-per-class))

;;*** Number of Children

(defn subtypes
  "Returns all direct subtypes of the given type t that are contained in
  classes."
  [classes t]
  ;; Well, this is pretty slow, because all the needed references are
  ;; unidirectional.  You can get the superclass quickly, but getting
  ;; subclasses is hardly possible.
  (mapcat (fn [c]
            (let [supers (reachables c [p-seq
                                        [p-alt :implements :extends]
                                        [p-opt :classifierReferences]
                                        :target])]
              (when (member? t supers)
                [c])))
          classes))

(defn classes-by-number-of-children
  [m]
  (let [all-classes (*get-classes-fn* m)]
    (apply-metric m #(count (subtypes all-classes %)))))

;;*** Response for a Class

(defn methods-of-class [c]
  (reachables c [p-seq  :members
                 [p-restr 'java.members.ClassMethod]]))

(defn response-set
  "Returns the response set of the given type t."
  [t]
  (let [own-methods (methods-of-class t)
        inherited-methods (mapcat methods-of-class
                                  (reachables t [p-seq :extends
                                                 [p-opt :classifierReferences]
                                                 :target]))
        all-methods (into own-methods inherited-methods)
        called-methods (mapcat
                        #(reachables % [p-seq :statements
                                        [p-* <>--]
                                        [p-restr 'java.references.MethodCall]
                                        :target])
                        all-methods)]
    (into all-methods called-methods)))

(defn classes-by-response-for-a-class
  [g]
  (apply-metric g #(count (response-set %))))

;;*** Lack of Cohesion in Object Methods

(defn lack-of-cohesion
  "Returns the lack of cohesion metric value of t."
  [t]
  (let [fields (reachables t [p-seq :members
                              [p-restr 'java.members.Field]])
        methods (reachables t [p-seq :members
                               [p-restr 'java.members.ClassMethod]])
        accessed-fields (fn [m]
                          (reachables m [p-seq :statements
                                         [p-* <>--]
                                         [p-restr 'java.references.IdentifierReference]
                                         :target
                                         [p-restr nil #(member? % fields)]]))
        method-field-map (apply hash-map (mapcat (fn [m] [m (accessed-fields m)])
                                                 methods))
        combinations (loop [ms methods, pairs []]
                       (if (next ms)
                         (recur (rest ms)
                                (concat pairs
                                        (map (fn [n] [(first ms) n])
                                             (rest ms))))
                         pairs))
        results (for [[m1 m2] combinations
                      :let [f1 (method-field-map m1)
                            f2 (method-field-map m2)]]
                  (if (seq (clojure.set/intersection f1 f2))
                    :common-fields
                    :disjoint-fields))
        p (count (filter #(= % :disjoint-fields) results))
        q (- (count results) p)]
    (if (> p q)
      (- p q)
      0)))

(defn classes-by-lack-of-cohesion-in-methods
  [g]
  (apply-metric g lack-of-cohesion))
