(ns component-study.components.config
  (:require [com.stuartsierra.component :as component]))

(defn get-user [env]
  (str "cesar-alcancio-" env))

(defrecord Config [env]
  component/Lifecycle

  (start [this]
    (println ";; Starting Config")
    (-> this
        (assoc :admin (get-user env))
        (assoc :env env)))

  (stop [this]
    (println ";; Stopping Config")
    this))

(defn new-config [env]
  (map->Config {:env env}))
