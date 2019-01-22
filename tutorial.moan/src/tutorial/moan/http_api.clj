(ns tutorial.moan.http-api
  (:require
    [integrant.core :as ig]
    [yada.yada :as yada]
    [clojure.walk :as walk]))

(defn- resolve-params
  [m pull]
  (reduce-kv
    (fn [pulled k v]
      (assoc pulled k (get-in m v)))
    pull
    pull))

(defn- api-resource
  [res]
  (yada/resource
    (update res :methods
            (fn [methods]
              (reduce-kv
                (fn [methods k v]
                  (let [sym (::symbol v)]
                    (require (symbol (namespace sym)))

                    (-> (walk/postwalk
                          (fn [x]
                            (if (= 'String x)
                              String
                              x))
                          methods)

                        (update-in [k :produces]
                                   (fnil conj #{})
                                   "application/edn")
                        (assoc-in [k :response]
                                  (fn [ctx]
                                    (def ctx ctx)
                                    (if-let [params (::params v)]
                                      ((resolve sym)
                                       (resolve-params ctx params))
                                      ((resolve sym))))))))
                methods
                methods)))))

(defmethod ig/init-key ::api-resource
  [_ res]
  (api-resource res))
