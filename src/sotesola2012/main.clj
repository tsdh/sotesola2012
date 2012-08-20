(ns sotesola2012.main
  (:use funnyqt.emf)
  (:require
   [clojure.string         :as str]
   [sotesola2012.metrics   :as metrics]
   [sotesola2012.util      :as util]))

(defn stringify-metric [metric model]
  (apply str
         (for [[v n] (metric model)]
           (str v "\t" n "\n"))))

(defn calculate-metrics [model metricsfile]
  (binding [metrics/*get-classes-fn* (fn [m]
                                       (filter util/internal-classifier?
                                               (eallobjects m '[Class])))]
    (spit
     metricsfile
     (str
      "Depth Of Inheritance Tree\n"
      "=========================\n\n"
      (stringify-metric metrics/classes-by-depth-of-inheritance-tree model)
      "\n"
      "Coupling Between Objects\n"
      "========================\n\n"
      (stringify-metric metrics/classes-by-coupling-between-objects model)
      "\n"
      "Weighted Methods Per Class\n"
      "==========================\n\n"
      (stringify-metric metrics/classes-by-weighted-methods-per-class model)
      "\n"
      "Number Of Children\n"
      "==================\n\n"
      (binding [metrics/*get-classes-fn* (fn [m]
                                           (filter util/internal-classifier?
                                                   (eallobjects m '[Interface Class])))]
        (stringify-metric metrics/classes-by-number-of-children model))
      "\n"
      "Response For A Class\n"
      "====================\n\n"
      (stringify-metric metrics/classes-by-response-for-a-class model)
      "\n"
      "Lack Of Cohesion in Object Methods\n"
      "==================================\n\n"
      (stringify-metric metrics/classes-by-lack-of-cohesion-in-methods model)))))

(defn do-all []
  (doseq [mf (util/files-in "models" #".*\.xmi$")
          :let [model (load-model mf)
                xmifile (.getName mf)
                metricsfile (str "output/" (str/replace xmifile ".xmi" ".txt"))]]
    (try
      (println "Processing" mf)
      (print "    Calculating metrics...")
      (calculate-metrics model metricsfile)
      (println "done!")
      (catch Exception _
        (println "FAILED!")))))

(defn -main []
  (do-all))

