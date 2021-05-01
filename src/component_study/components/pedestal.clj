(ns component-study.components.pedestal
  (:require [com.stuartsierra.component :as component]
            [component-study.components.database :as database]
            [component-study.components.example-component :as example-component]
            [component-study.components.scheduler :as scheduler]
            [clojure.pprint :as pp]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [io.pedestal.interceptor :as i]))

(defrecord Pedestal [routes database app]
  component/Lifecycle

  (start [this]
    (println ";; Pedestal OI")

    (println routes)

    (def service-map
      {::http/routes (:local-routes routes)
       ::http/type   :jetty
       ::http/port   8890})

    (def service-map-nu (update-in
                          (http/default-interceptors service-map)
                          [::http/interceptors]
                          #(vec (cons
                                  (i/interceptor
                                    {:name  ::system
                                     :enter (fn [context]
                                              (assoc-in context [:request :components] {:routes routes :database database :app app}))}) %))))

    (def service-map-doc (-> service-map
                             (http/default-interceptors)
                             (update ::http/interceptors conj (i/interceptor {:name  ::system
                                                                              :enter (fn [context]
                                                                                       (assoc-in context
                                                                                                 [:request :components]
                                                                                                 {:routes routes :database database :app app}))}))))

    (defn start []
      (http/start (http/create-server service-map-doc)))

    ;; For interactive development
    (defonce server (atom nil))

    (defn start-dev []
      (reset! server
              (http/start (http/create-server
                            (assoc service-map-doc
                              ::http/join? false)))))

    (defn stop-dev []
      (http/stop @server))

    (defn restart []
      (stop-dev)
      (start-dev))


    (try (start-dev) (catch Exception e (println "Error!" e)))
    (try (restart) (catch Exception e (println "Error!" e)))

    (defn test-request [verb url]
      (io.pedestal.test/response-for (::http/service-fn @server) verb url))

    (-> this
        (assoc :result (test-request :get "/test-3"))
        (assoc :test-request test-request)
        (assoc :start-dev start-dev)
        (assoc :restart-dev restart)
        (assoc :server server)
        (assoc :pedestal "pedestal ok!!")))

  (stop [this]
    this))

(defn new-pedestal []
  (map->Pedestal {}))
