(ns component-study.service
  (:require [com.stuartsierra.component :as component]
            [component-study.components.database :as database]
            [component-study.components.example-component :as example-component]
            [component-study.components.scheduler :as scheduler]
            [clojure.pprint :as pp]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [io.pedestal.interceptor :as i]))

; Pedestal
(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok (partial response 200))
(def created (partial response 201))
(def accepted (partial response 202))

(defn find-list-by-id [dbval db-id]
  (get dbval db-id))

(defn find-list-item-by-ids [dbval list-id item-id]
  (get-in dbval [list-id :items item-id] nil))

(defn list-item-add
  [dbval list-id item-id new-item]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(defn make-list [nm]
  {:name  nm
   :items {}})

(defn make-list-item [nm]
  {:name  nm
   :done? false})

;;;
;;; API Interceptors
;;;
(def echo
  {:name :echo
   :enter
         (fn [context]
           (let [request (:request context)
                 response (ok context)]
             (assoc context :response response)))})

(def entity-render
  {:name :entity-render
   :leave
         (fn [context]
           (if-let [item (:result context)]
             (assoc context :response (ok item))
             context))})

(def list-create
  {:name :list-create
   :enter
         (fn [context]
           (let [nm (get-in context [:request :query-params :name] "Unnamed List")
                 new-list (make-list nm)
                 db-id (str (gensym "l"))
                 url (route/url-for :list-view :params {:list-id db-id})]
             (assoc context
               :response (created new-list "Location" url)
               :tx-data [assoc db-id new-list])))})

(def list-view
  {:name :list-view
   :enter
         (fn [context]
           (if-let [db-id (get-in context [:request :path-params :list-id])]
             (if-let [the-list (find-list-by-id (get-in context [:request :database]) db-id)]
               (assoc context :result the-list)
               context)
             context))})

(def list-item-view
  {:name :list-item-view
   :leave
         (fn [context]
           (if-let [list-id (get-in context [:request :path-params :list-id])]
             (if-let [item-id (get-in context [:request :path-params :item-id])]
               (if-let [item (find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
                 (assoc context :result item)
                 context)
               context)
             context))})

(def list-item-create
  {:name :list-item-create
   :enter
         (fn [context]
           (if-let [list-id (get-in context [:request :path-params :list-id])]
             (let [nm (get-in context [:request :query-params :name] "Unnamed Item")
                   new-item (make-list-item nm)
                   item-id (str (gensym "i"))]
               (-> context
                   (assoc :tx-data [list-item-add list-id item-id new-item])
                   (assoc-in [:request :path-params :item-id] item-id)))
             context))})

(def test-um
  {:name :test-um
   :enter
         (fn [context]
           (-> context
               (update :request assoc :database (:database context))
               (update :request assoc :my-name "Cesar")))})

(def test-dois
  {:name  :test-dois
   :enter (fn [context]
            (println "Running test 2")
            (println context)
            (println (-> context :request :my-name))
            (assoc context :response {:status 200 :body (-> context :request :my-name)}))})

(defn test-tres [request]
  (println "Running test 3")
  (println request)
  (println (-> request :components :app :admin))
  {:status 200 :body (str "test tres equals 2 but different: " (-> request :my-name) " " (-> request :components :app :admin))})

(def db-interceptor
  {:name :database-interceptor
   :enter
         (fn [context]
           (let [database (:database (:database (:components (:request context))))]
             (update context :request assoc :database @database)))
   :leave
         (fn [context]
           (let [database (:database (:database (:components (:request context))))]
             (if-let [[op & args] (:tx-data context)]
               (do
                 (apply swap! database op args)
                 (assoc-in context [:request :database] @database))
               context)))})

(defn list-todos [request]
  (ok @(-> request :components :database :database)))

(def routes
  (route/expand-routes
    #{["/todo" :post [db-interceptor list-create]]
      ["/todo" :get [list-todos] :route-name :list-query-form]
      ["/todo/:list-id" :get [entity-render db-interceptor list-view]]
      ["/todo/:list-id" :post [entity-render list-item-view db-interceptor list-item-create]]
      ["/todo/:list-id/:item-id" :get [entity-render list-item-view db-interceptor]]
      ["/todo/:list-id/:item-id" :put echo :route-name :list-item-update]
      ["/todo/:list-id/:item-id" :delete echo :route-name :list-item-delete]
      ["/test-1" :get [test-um test-dois]]
      ["/test-3" :get [test-tres] :route-name :test-tres]}))