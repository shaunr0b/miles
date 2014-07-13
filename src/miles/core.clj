(ns miles.core
  (:require
   [org.httpkit.server :refer (run-server)]
   [com.stuartsierra.component :as component]
   [taoensso.timbre :refer (info)]))


;;;;; WEB SERVER

(defrecord Server
  [port make-handler-fn]

  component/Lifecycle

  (start [component]
         (info "Starting web server on port " port)
         (assoc component :server (run-server
                                     (make-handler-fn)
                                     {:port port})))

  (stop [component]
        (info "Stopping web server on port " port)
        ((:server component)) ; stop the running org.httpkit.server
        (dissoc component :server)))


(defn new-server [port handler-fn]
  (->Server port handler-fn))


;;;;; WEB SERVER

(defn make-handler []
  (fn [req]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    "hello HTTP!"}))

;; Start Web Server
(def web-server (atom nil))

(defn stop []
  (when @web-server
    (component/stop @web-server)
    (reset! web-server nil)))

(defn start
  "Stop the web server, if started. Start a new web server. "
  []
  (stop)
  (reset! web-server (component/start (new-server 3000 make-handler))))

