(ns component-study.components.scheduler
  (:require [com.stuartsierra.component :as component]))

(defrecord Scheduler []
  component/Lifecycle

  (start [this]
    (println ";; Starting Scheduler")
    (assoc this :scheduler "MyScheduler"))

  (stop [this]
    (println ";; Stopping ExampleComponent")
    this))

(defn new-scheduler []
  (->Scheduler))
