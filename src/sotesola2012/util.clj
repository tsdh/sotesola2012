(ns sotesola2012.util
  (:use funnyqt.emf
        funnyqt.protocols)
  (:require [clojure.java.io      :as io]
            [sotesola2012.metrics :as metrics]))

(def jamopp-init
  (memoize
   (fn []
     (load-metamodel "metamodels/layout.ecore")
     (load-metamodel "metamodels/java.ecore"))))

(jamopp-init)

(defn files-in
  "The seq of all files in dir."
  [dir rx]
  (jamopp-init)
  (remove (fn [^java.io.File f]
            (or (.isDirectory f)
                (not (re-matches rx (.getName f)))))
          (file-seq (io/file dir))))

(defn internal-classifier?
  [c]
  (and
   (eget c :name) ;; exclude anonymous classes
   (let [qn (metrics/classifier-qname c)]
     (or
      ;; A whitelist of packages extracted from 101repo/contributions
      (re-matches
       #"^(atl|com\.softlang|company|de\.uni_koblenz\.oneoonecompanies|emfreflexive|javaf|org\.ioicompanies|org\.softlang|org\.yapg|pkg101|parseLib)\..*"
       qn)
      ;; Also allow classes in the default pkg
      (re-matches #"^[A-Z][^.]*" qn)))))

(defn internal-classifiers [jm]
  (set (filter internal-classifier?
               (eallobjects jm '[Class Interface]))))

