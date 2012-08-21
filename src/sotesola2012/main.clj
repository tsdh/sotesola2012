(ns sotesola2012.main
  (:use funnyqt.emf)
  (:require
   [clojure.string         :as str]
   [sotesola2012.visualize :as viz]
   [sotesola2012.metrics   :as metrics]
   [sotesola2012.util      :as util]))

(defn stringify-metric [metric model]
  (apply str
         (for [[v n] (metric model)]
           (str v "\t" n "\n"))))

(defn classes-only [m]
  (filter util/internal-classifier?
          (eallobjects m '[Class])))

(defn classes-and-ifaces [m]
  (filter util/internal-classifier?
          (eallobjects m '[Class Interface])))

(defn calculate-metrics [model metricsfile]
  (binding [metrics/*get-classes-fn* classes-only]
    (spit
     metricsfile
     (str
      "Depth Of Inheritance Tree\n"
      "=========================\n\n"
      (binding [metrics/*get-classes-fn* classes-and-ifaces]
        (stringify-metric metrics/classes-by-depth-of-inheritance-tree model))
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
      (binding [metrics/*get-classes-fn* classes-and-ifaces]
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
                metricsfile (str "output/" (str/replace xmifile ".xmi" ".txt"))
                dotfile (str "output/" (str/replace xmifile ".xmi" ".dot"))
                pdffile (str "output/" (str/replace xmifile ".xmi" ".pdf"))]]
    (try
      (println "Processing" mf)
      (print "    Calculating metrics...")
      (calculate-metrics model metricsfile)
      (println "done!")
      (print "    Generating DOT visualization...")
      (viz/generate-dot model dotfile)
      (println "done!")
      (print "    Generating PDF...")
      (if (= 0 (:exit (clojure.java.shell/sh
                       "dot" "-Tpdf" "-o" pdffile dotfile)))
        (println "done!")
        (println "FAILED!"))
      (clojure.java.shell/sh "rm" dotfile)
      (catch Exception e
        (println "FAILED!")
        (.printStackTrace e)))))

(defn -main []
  (do-all))

