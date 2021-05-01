(ns component-study.components.database
  (:require [com.stuartsierra.component :as component]))

(defonce database (atom {}))

(defrecord Database []
  component/Lifecycle

  (start [component]
    (println ";; Starting database")
    (-> component
        (assoc :database database)))

  (stop [component]
    (println ";; Stopping database")
    component))

(defn new-database []
  (map->Database {}))
