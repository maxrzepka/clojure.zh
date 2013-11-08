(ns zh-async.core
  (:require [clojure.core.async :as a]))

#_ (let [c (a/chan)]
     (a/go (a/>! c (rand-int 100)))
     (a/go
      (println "Before take")
      (println "Take it : " (a/<! c))
      ))

(defn my-ints
  ([]
     (let [out (a/chan)]
       (a/go (loop [i 1]
               (a/>! out i)
               (recur (inc i))))
       out))
  ([ms]
     (let [out (a/chan)]
       (a/go (loop [i 1]
               (a/>! out i)
               (a/timeout ms)
               (recur (inc i))))
       out)))

#_(defn my-ints [ms]
  (let [out (a/chan (a/sliding-buffer 3))]
    (a/go (loop [i 1]
            (a/>! out i)
            (a/timeout ms)
          (recur (inc i))))
    out))

#_ (let [ints (my-ints)]
    (doseq [i (range 10)]
      (a/go
       (let [v (a/<! ints)] (println i " " v)))))

;; Pub/Sub

(defn events [file]
  (map read-string (.split (slurp file) "\n")))

(defn ->channel [coll]
  (let [out (a/chan ;(a/sliding-buffer (count coll))
             )]
    (a/onto-chan out coll)
    out))

(defn publish [events]
  (a/pub events
         (fn [e] (.toLowerCase (first (:tags e))))))


;; Internals explained :  Timothy Baldridge screencast
;; http://www.youtube.com/channel/UCLxWPHbkxjR-G-y6CVoEHOw?feature=watch

;; gen-plan : a state monad ... just some high-order functions

(defn add-name [name]
  (fn [m]
    (assoc m :name name)))

(defn add-city [city]
  (fn [m]
    (assoc m :city city)))

#_ ((comp (add-name "Clojure") (add-city "NYC")))

#_(-> m 
    (assoc-in [:a] "one")
    (assoc-in [:b] "two")
    (assoc-in [:c] "three")
    )
;; expands to (nrepl.el shortcuts C-c RET , C-c M-m )
#_(assoc-in
   (assoc-in
    (assoc-in m [:a] "one")
    [:b] "two")
   [:c] "three")

#_(->> coll
     (filter even?)
     (map inc))

#_(as-> x )

#_(cond->)

;; http://hueypetersen.com/posts/2013/08/02/the-state-machines-of-core-async/
;; To get the state machine behind or expand the go macro
#_(clojure.pprint/pprint
 (clojure.core.async.impl.ioc-macros/state-machine
  '(let [c (chan)] (go (<! c 5))) 1 {} {}))
