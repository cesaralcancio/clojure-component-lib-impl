(ns component-study.main
  (:require [component-study.components :as components]
            [clojure.pprint :as pp]
            [io.pedestal.test :as test]
            [io.pedestal.http :as http]))

(def result (components/start-dev))
; (def result (components/start-prod))

(def server (-> result :pedestal :server))
(defn test-request [verb url]
  (io.pedestal.test/response-for (::http/service-fn @server) verb url))

; URI de Teste
(println (test-request :get "/version"))

; Provando os serviços
(def todo-cesar (test-request :post "/todo?name=cesar-alcancio-todo-list"))
(def location-cesar (-> todo-cesar :headers (get "Location")))

(println (test-request :get "/todo"))

(println (test-request :post (str location-cesar "?name=cesar-item-1")))
(println (test-request :get location-cesar))

