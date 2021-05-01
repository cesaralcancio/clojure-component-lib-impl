(ns component-study.components
  (:require [com.stuartsierra.component :as component]
            [component-study.components.database :as database]
            [component-study.components.pedestal :as component.pedestal]
            [component-study.components.routes :as routes]
            [component-study.components.config :as config]
            [component-study.components.webapp :as webapp]))

(defn base-system [env]
  (component/system-map
    :config (config/new-config env)
    :database (database/new-database)
    :routes (routes/new-routes)
    :webapp (component/using (webapp/new-webapp) [:config :database :routes])
    :pedestal (component/using (component.pedestal/new-pedestal) [:config :database :routes :webapp])))

(defn start-prod []
  (let [system-return (component/start (base-system :prod))
        start (-> system-return :pedestal :start)]
    (start)))

(defn start-dev []
  (let [system-return (component/start (base-system :dev))
        start-dev (-> system-return :pedestal :start-dev)
        restart (-> system-return :pedestal :restart)]
    (try (start-dev) (catch Exception e (try (restart) (catch Exception e (println "Error!" e)))))
    system-return))
