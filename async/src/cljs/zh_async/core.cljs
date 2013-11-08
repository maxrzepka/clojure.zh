(ns zh-async.core
  (:require [cljs.core.async :as a]
            [goog.dom :as dom])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

;; from https://github.com/swannodette/hs-async/blob/master/src/hs_async/core.cljs

#_ (.log js/console (go 5))

#_ (.log js/console (a/<! (go 5)))

#_ (go (.log js/console (a/<! (go 5))))

#_ (let [c (a/chan)]
     (go
      (.log js/console "We got here")
      (a/<! c)
      (.log js/console "We'll never get here")))

#_ (let [c (a/chan)]
     (go
      (.log js/console "We got here")
      (a/<! c)
      (.log js/console "We made progress"))
     (go
      (a/>! c (js/Date.))))

#_ (let [c (a/chan)]
     (go
      (a/>! c (js/Date.)))
     (go
      (.log js/console "Order")
      (a/<! c)
      (.log js/console "doesn't matter")))


#_ (let [c (a/chan)
         t (a/timeout 1000)]
     (go
      (let [[value selected-channel] (alts! [c t])]
        (.log js/console "Timeout channel closed!" (= selected-channel t)))))

#_ (let [c (a/chan)
         t (a/timeout 1000)]
     (go
      (alt!
       c ([v] (.log js/console "Channel responded"))
       t ([v] (.log js/console "Timeout channel closed!")))))

;; Event loop

(defn events [el type]
  (let [out (a/chan)]
    (.addEventListener el type
                       (fn [e] (a/put! out e)))
    out))

#_(let [move (events js/window "mousemove")]
     (go (while true
           (.log js/console (a/<! move)))))

#_(defn filter! [pred in]
  (let [out (a/chan)]
    (go (while true
          (let [x (a/<! in)]
            (when (pred x)
              (a/>! out x)))))
    out))

(defn x-mod-5-y-mod-10 [e]
  (let [[x y] [(.-pageX e) (.-pageY e)]]
    (and (zero? (mod x 5))
        (zero? (mod y 10)))))

(let [filtered (a/filter< x-mod-5-y-mod-10
                         (events js/window "mousemove"))]
     (go (while true
           (.log js/console (a/<! filtered)))))

;; Coordination

(defn fan-in [ins]
  (let [out (a/chan)]
    (go (while true
          (let [[x] (alts! ins)]
            (a/>! out x))))
    out))

(defn my-ints []
  (let [out (a/chan)]
    (go (loop [i 1]
          (a/>! out i)
          (recur (inc i))))
    out))

(defn interval [msecs]
  (let [out (a/chan)]
    (go (while true
          (a/>! out (js/Date.))
          (a/<! (a/timeout msecs))))
    out))

(defn process [name control]
  (let [out  (a/chan)
        ints (my-ints)
        tick (interval 1000)]
    (go
     (a/<! control)
     (.log js/console "start" name)
     (loop [acc 0]
       (let [[v c] (alts! [tick control])]
         (condp = c
           control (do (.log js/console "pause" name)
                       (a/<! control)
                       (.log js/console "continue" name)
                       (recur acc))
           (do
             (a/>! out [name acc])
             (recur (+ acc (a/<! ints))))))))
    out))

(defn now [] (js/Date.))

#_ (let [c0   (a/chan)
         c1   (a/chan)
         out  (fan-in [(process "p0" c0) (process "p1" c1)])
         keys (->> (events js/window "keyup")
                   (map #(.-keyCode %))
                   (filter #{32}))]
     (go
      (a/>! c0 (now))
      (loop [state 0]
        (recur
         (alt!
          out
          ([v] (do (.log js/console (pr-str v)) state))

          keys
          ([v] (case state
                 0 (do (a/>! c0 (now)) (a/>! c1 (now)) 1)
                 1 (do (a/>! c1 (now)) (a/>! c0 (now)) 0))))))))




