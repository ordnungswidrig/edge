(ns edge.kick.builder
  (:require
    [integrant.core :as ig]
    [juxt.kick.alpha.core :as kick]))

(defn load-provider-namespaces
  [kick-config]
  (doseq [provider (keys kick-config)
          :when (= (namespace provider) "kick")]
    (when (= (namespace provider) "kick")
      (let [sym (symbol (str "juxt.kick.alpha.providers." (name provider)))]
        (try (do (require sym) sym)
             (catch java.io.FileNotFoundException _))))))

(defmethod ig/init-key :edge.kick/builder
  [_ v]
  (load-provider-namespaces v)
  (kick/watch v))

(defmethod ig/halt-key! :edge.kick/builder
  [_ close]
  (close))

(defmethod ig/suspend-key! :edge.kick/builder [_ _])

(defmethod ig/resume-key :edge.kick/builder
  [key opts old-opts old-impl]
  (if (= opts old-opts)
    old-impl
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))
