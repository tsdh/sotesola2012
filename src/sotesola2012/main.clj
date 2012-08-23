(ns sotesola2012.main
  (:use funnyqt.emf)
  (:require
   [funnyqt.query          :as q]
   [clojure.string         :as str]
   [sotesola2012.visualize :as viz]
   [sotesola2012.metrics   :as metrics]
   [sotesola2012.util      :as util]))

(defn gimme-sorted-set []
  (sorted-set-by (q/seq-compare #(- %2 %1) compare compare)))

(def overall-scores (atom {:DIT  (gimme-sorted-set)
                           :CBO  (gimme-sorted-set)
                           :WMC  (gimme-sorted-set)
                           :NOC  (gimme-sorted-set)
                           :RFC  (gimme-sorted-set)
                           :LCOM (gimme-sorted-set)}))

(defn stringify-metric [metric-kw metric model implname]
  (apply str
         (for [[v n] (metric model)]
           (do
             (swap! overall-scores
                    #(update-in %1 [metric-kw] conj [v n implname]))
             (str v "\t" n "\n")))))

(defn classes-only [m]
  (filter util/internal-classifier?
          (eallobjects m '[Class])))

(defn classes-and-ifaces [m]
  (filter util/internal-classifier?
          (eallobjects m '[Class Interface])))

(defn calculate-metrics [model metricsfile implname]
  (binding [metrics/*get-classes-fn* classes-only]
    (spit
     metricsfile
     (str
      "Depth Of Inheritance Tree\n"
      "=========================\n\n"
      (binding [metrics/*get-classes-fn* classes-and-ifaces]
        (stringify-metric :DIT metrics/classes-by-depth-of-inheritance-tree model implname))
      "\n"
      "Coupling Between Objects\n"
      "========================\n\n"
      (stringify-metric :CBO metrics/classes-by-coupling-between-objects model implname)
      "\n"
      "Weighted Methods Per Class\n"
      "==========================\n\n"
      (stringify-metric :WMC metrics/classes-by-weighted-methods-per-class model implname)
      "\n"
      "Number Of Children\n"
      "==================\n\n"
      (binding [metrics/*get-classes-fn* classes-and-ifaces]
        (stringify-metric :NOC metrics/classes-by-number-of-children model implname))
      "\n"
      "Response For A Class\n"
      "====================\n\n"
      (stringify-metric :RFC metrics/classes-by-response-for-a-class model implname)
      "\n"
      "Lack Of Cohesion in Object Methods\n"
      "==================================\n\n"
      (stringify-metric :LCOM metrics/classes-by-lack-of-cohesion-in-methods model implname)))))

(defn write-overall-scores-file []
  (spit "output/OVERALL_SCORES.txt"
        (apply str
               (for [k (keys @overall-scores)]
                 (str "The 10 most complex classes according to metric " (name k) "\n\n"
                      (format  "%5s | %15s | %s\n" "Score" "Implementation" "Class/Interface name")
                      (format  "%5s + %15s + %s\n" "-----" "--------------" "--------------------")
                      (apply str
                             (for [[s c i] (take 10 (k @overall-scores))]
                               (format "%5s | %15s | %s\n" s i c)))
                      "\n")))))

(defn do-all []
  (doseq [mf (util/files-in "models" #".*\.xmi$")
          :let [model (load-model mf)
                xmifile (.getName mf)
                implname (str/replace xmifile ".xmi" "")
                metricsfile (str "output/" (str/replace xmifile ".xmi" ".txt"))
                dotfile (str "output/" (str/replace xmifile ".xmi" ".dot"))
                pdffile (str "output/" (str/replace xmifile ".xmi" ".pdf"))]]
    (try
      (println "Processing" mf)
      (print "    Calculating metrics...")
      (calculate-metrics model metricsfile implname)
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
        (.printStackTrace e))))
  (print "Writing overall scores file...")
  (write-overall-scores-file)
  (println "done!"))

(defn -main []
  (do-all))

