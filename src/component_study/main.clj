(ns component-study.main
  (:require [component-study.components :as components]
            [clojure.pprint :as pp]
            [io.pedestal.test :as test]))

(def result (components/main))

(def resultado-api (-> result :pedestal :result))
(pp/pprint resultado-api)

(def test-request (-> result :pedestal :test-request))

(test-request :get "/test-3")
(test-request :post "/todo?name=ZZZZ-List")
(test-request :get "/todo")
