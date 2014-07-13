(defproject miles "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-pro "0.9.4815.12"]
                 [com.stuartsierra/component "0.2.1"]
                 [http-kit "2.1.16"]
                 [cheshire "5.3.1"]
                 [compojure "1.1.8"]
                 [com.taoensso/timbre "3.2.1"]]

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username "tvanhens@gmail.com"
                                   :password "08c077a7-3266-4b1e-8765-2befc5e93d0f"}
                 "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
