(ns component-study.components.example-component
  (:require [com.stuartsierra.component :as component]))

(defn get-user [database role]
  (str "cesar-alcancio-database-" (:database database) "-with-" role "-role"))

(defrecord ExampleComponent [options cache database scheduler]
  component/Lifecycle

  (start [this]
    (println ";; Starting ExampleComponent")
    (assoc this :admin (get-user database "admin")))

  (stop [this]
    (println ";; Stopping ExampleComponent")
    this))

(defn new-example-component [config-options]
  (map->ExampleComponent {:options config-options
                          :cache   (atom {})}))
