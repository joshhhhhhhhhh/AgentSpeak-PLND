(define (problem alban)
  (:domain d1)
(:objects
    x_0 - X
    x_1 - X
    x_2 - X
    x_3 - X
    x_4 - X
    y_0 - y
    y_1 - y
    y_2 - y
    y_3 - y
    y_4 - y
  )
  (:init (and
    (alive)
    (can_move)
    (oneof
        ((atx x_1) (aty y_1))
        ((atx x_2) (aty y_1))
        ((atx x_3) (aty y_1))
        ((atx x_1) (aty y_2))
        ((atx x_2) (aty y_2))
        ((atx x_3) (aty y_2))
        ((atx x_1) (aty y_3))
        ((atx x_2) (aty y_3))
        ((atx x_3) (aty y_3))
    )))
  (:goal (and (alive) (atx x_2) (aty y_2)))
)