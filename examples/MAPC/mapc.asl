range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

~loc(X1, Y1) :- loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).

~obs(D) :- not obs(D).

// Obstacle mappings
obs(left) :- loc(0,2).
obs(down) :- loc(1,1).
obs(up) :- loc(1,3).
obs(down) :- loc(2,1).
obs(up) :- loc(2,3).
obs(right) :- loc(3,2).

// No obstacles otherwise (closed-world)
~obs(D) :- not(obs(D)).

+on(obs(Dir)) : obs(Dir).
+on(~obs(Dir)) : ~obs(Dir).

!des.

+!des[source(self)] : desires(Goal) <-
    .print("STARTING PLANNER");
    org.soton.peleus.act.plan(Goal, [makeGeneric(false)]);
    .print("PLANNER COMPLETE").

// "On" plans for movement
+on(moved(right))
    : loc(X, Y) & X < 4
    <-  -loc(X, Y);
        +loc(X + 1, Y).

+on(moved(left))
    : loc(X, Y) & X > 0
    <-  -loc(X, Y);
        +loc(X - 1, Y).

+on(moved(up))
    : loc(X, Y) & Y > 0
    <-  -loc(X, Y);
        +loc(X, Y - 1).

+on(moved(down))
    : loc(X, Y) & Y < 4
    <-  -loc(X, Y);
        +loc(X, Y + 1).


@action1 +!move_up <-
    WHEN_START;
    +loc(x_0,y_1);
    WHEN_BREAK;
    -loc(x_0,y_1);
    +loc(x_0,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_1);
    WHEN_BREAK;
    -loc(x_1,y_1);
    +loc(x_1,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_1);
    WHEN_BREAK;
    -loc(x_2,y_1);
    +loc(x_2,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_1);
    WHEN_BREAK;
    -loc(x_3,y_1);
    +loc(x_3,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_1);
    WHEN_BREAK;
    -loc(x_4,y_1);
    +loc(x_4,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_2);
    WHEN_BREAK;
    -loc(x_0,y_2);
    +loc(x_0,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_2);
    WHEN_BREAK;
    -loc(x_3,y_2);
    +loc(x_3,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_2);
    WHEN_BREAK;
    -loc(x_4,y_2);
    +loc(x_4,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_3);
    WHEN_BREAK;
    -loc(x_0,y_3);
    +loc(x_0,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_3);
    WHEN_BREAK;
    -loc(x_3,y_3);
    +loc(x_3,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_3);
    WHEN_BREAK;
    -loc(x_4,y_3);
    +loc(x_4,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_4);
    WHEN_BREAK;
    -loc(x_0,y_4);
    +loc(x_0,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_4);
    WHEN_BREAK;
    -loc(x_1,y_4);
    +loc(x_1,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_4);
    WHEN_BREAK;
    -loc(x_2,y_4);
    +loc(x_2,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_4);
    WHEN_BREAK;
    -loc(x_3,y_4);
    +loc(x_3,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_4);
    WHEN_BREAK;
    -loc(x_4,y_4);
    +loc(x_4,y_3);
    WHEN_END.

@action2 +!move_down <-
    WHEN_START;
    +loc(x_0,y_0);
    WHEN_BREAK;
    -loc(x_0,y_0);
    +loc(x_0,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_0);
    WHEN_BREAK;
    -loc(x_1,y_0);
    +loc(x_1,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_0);
    WHEN_BREAK;
    -loc(x_2,y_0);
    +loc(x_2,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_0);
    WHEN_BREAK;
    -loc(x_3,y_0);
    +loc(x_3,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_0);
    WHEN_BREAK;
    -loc(x_4,y_0);
    +loc(x_4,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_1);
    WHEN_BREAK;
    -loc(x_0,y_1);
    +loc(x_0,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_1);
    WHEN_BREAK;
    -loc(x_3,y_1);
    +loc(x_3,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_1);
    WHEN_BREAK;
    -loc(x_4,y_1);
    +loc(x_4,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_2);
    WHEN_BREAK;
    -loc(x_0,y_2);
    +loc(x_0,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_2);
    WHEN_BREAK;
    -loc(x_3,y_2);
    +loc(x_3,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_2);
    WHEN_BREAK;
    -loc(x_4,y_2);
    +loc(x_4,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_3);
    WHEN_BREAK;
    -loc(x_0,y_3);
    +loc(x_0,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_3);
    WHEN_BREAK;
    -loc(x_1,y_3);
    +loc(x_1,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_3);
    WHEN_BREAK;
    -loc(x_2,y_3);
    +loc(x_2,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_3);
    WHEN_BREAK;
    -loc(x_3,y_3);
    +loc(x_3,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_3);
    WHEN_BREAK;
    -loc(x_4,y_3);
    +loc(x_4,y_4);
    WHEN_END.

@action3 +!move_right <-
    WHEN_START;
    +loc(x_0,y_0);
    WHEN_BREAK;
    -loc(x_0,y_0);
    +loc(x_0,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_1);
    WHEN_BREAK;
    -loc(x_0,y_1);
    +loc(x_1,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_3);
    WHEN_BREAK;
    -loc(x_0,y_3);
    +loc(x_1,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_0,y_4);
    WHEN_BREAK;
    -loc(x_0,y_4);
    +loc(x_1,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_0);
    WHEN_BREAK;
    -loc(x_1,y_0);
    +loc(x_2,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_1);
    WHEN_BREAK;
    -loc(x_1,y_1);
    +loc(x_2,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_3);
    WHEN_BREAK;
    -loc(x_1,y_3);
    +loc(x_2,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_4);
    WHEN_BREAK;
    -loc(x_1,y_4);
    +loc(x_2,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_0);
    WHEN_BREAK;
    -loc(x_2,y_0);
    +loc(x_3,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_1);
    WHEN_BREAK;
    -loc(x_2,y_1);
    +loc(x_3,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_3);
    WHEN_BREAK;
    -loc(x_2,y_3);
    +loc(x_3,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_4);
    WHEN_BREAK;
    -loc(x_2,y_4);
    +loc(x_3,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_0);
    WHEN_BREAK;
    -loc(x_3,y_0);
    +loc(x_4,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_1);
    WHEN_BREAK;
    -loc(x_3,y_1);
    +loc(x_4,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_2);
    WHEN_BREAK;
    -loc(x_3,y_2);
    +loc(x_4,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_3);
    WHEN_BREAK;
    -loc(x_3,y_3);
    +loc(x_4,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_4);
    WHEN_BREAK;
    -loc(x_3,y_4);
    +loc(x_4,y_4);
    WHEN_END.

@action4 +!move_left <-
    WHEN_START;
    +loc(x_1,y_0);
    WHEN_BREAK;
    -loc(x_1,y_0);
    +loc(x_0,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_1);
    WHEN_BREAK;
    -loc(x_1,y_1);
    +loc(x_0,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_3);
    WHEN_BREAK;
    -loc(x_1,y_3);
    +loc(x_0,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_1,y_4);
    WHEN_BREAK;
    -loc(x_1,y_4);
    +loc(x_0,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_0);
    WHEN_BREAK;
    -loc(x_2,y_0);
    +loc(x_1,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_1);
    WHEN_BREAK;
    -loc(x_2,y_1);
    +loc(x_1,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_3);
    WHEN_BREAK;
    -loc(x_2,y_3);
    +loc(x_1,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_2,y_4);
    WHEN_BREAK;
    -loc(x_2,y_4);
    +loc(x_1,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_0);
    WHEN_BREAK;
    -loc(x_3,y_0);
    +loc(x_2,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_1);
    WHEN_BREAK;
    -loc(x_3,y_1);
    +loc(x_2,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_3);
    WHEN_BREAK;
    -loc(x_3,y_3);
    +loc(x_2,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_3,y_4);
    WHEN_BREAK;
    -loc(x_3,y_4);
    +loc(x_2,y_4);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_0);
    WHEN_BREAK;
    -loc(x_4,y_0);
    +loc(x_3,y_0);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_1);
    WHEN_BREAK;
    -loc(x_4,y_1);
    +loc(x_3,y_1);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_2);
    WHEN_BREAK;
    -loc(x_4,y_2);
    +loc(x_3,y_2);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_3);
    WHEN_BREAK;
    -loc(x_4,y_3);
    +loc(x_3,y_3);
    WHEN_END;
    WHEN_START;
    +loc(x_4,y_4);
    WHEN_BREAK;
    -loc(x_4,y_4);
    +loc(x_3,y_4);
    WHEN_END.