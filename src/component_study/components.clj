(ns component-study.components
  (:require [com.stuartsierra.component :as component]
            [component-study.components.database :as database]
            [component-study.components.example-component :as example-component]
            [component-study.components.scheduler :as scheduler]
            [clojure.pprint :as pp]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [io.pedestal.interceptor :as i]
            [component-study.components.pedestal :as component.pedestal]
            [component-study.components.routes :as routes]))

(defn example-system [config-options]
  (let [{:keys [host port]} config-options]
    (component/system-map
      :scheduler (scheduler/new-scheduler)
      :app (component/using (example-component/new-example-component config-options) [:database :scheduler])

      :database (database/new-database)
      :routes (routes/new-routes)
      :pedestal (component/using (component.pedestal/new-pedestal) [:routes :database :app]))))

(def system (example-system {:host "localhost" :port 8080}))
(defn main [] (component/start system))
