range(posx(X)) :- .range(X, 0, 4).
range(posy(X)) :- .range(X, 0, 4).

range(noneX) :- true.
noneX :- .findall(not(posx(X)), range(posx(X)), List) & .big_and(Y, List) & Y.
~noneX.

range(noneY) :- true.
noneY :- .findall(not(posy(X)), range(posy(X)), List) & .big_and(Y, List) & Y.
~noneY.

~posx(X1) :- posx(X2) & (X1 \== X2).
~posy(X1) :- posy(X2) & (X1 \== X2).

// Obstacle mappings
obs(left, Z) :- posx(3) & posy(2).
obs(down, Z) :- posx(1) & posy(1).
obs(up, Z) :- posx(1) & posy(3).
obs(down, Z) :- posx(2) & posy(1).
obs(up, Z) :- posx(2) & posy(3).
obs(right, Z) :- posx(0) & posy(2).

// No obstacles otherwise (closed-world)
~obs(D, Z) :- not(obs(D, Z)).

+on(obs(Dir, Z)) : obs(Dir, Z).
+on(~obs(Dir, Z)) : ~obs(Dir, Z).

!des.

+!des[source(self)] : desires(Goal) <-
    .print("STARTING PLANNER");
    org.soton.peleus.act.plan(Goal, [makeGeneric(false), mixed(true)]);
    .print("PLANNER COMPLETE").

// "On" plans for movement
+on(moved(right, Z))
    : posx(X) & X < 4
    <-  -posx(X);
        +posx(X + 1).

+on(moved(left, Z))
    : posx(X) & X > 0
    <-  -posx(X);
        +posx(X - 1).

+on(moved(up, Z))
    : posy(Y) & Y > 0
    <-  -posy(Y);
        +posy(Y - 1).

+on(moved(down, Z))
    : posy(Y) & Y < 4
    <-  -posy(Y);
        +posy(Y + 1).


@action1 +!move_up <-
    WHEN_START;
    +posy(y_1);
    WHEN_BREAK;
    -posy(y_1);
    +posy(y_0);
    WHEN_END;
    WHEN_START;
    +posy(y_2);
    WHEN_BREAK;
    -posy(y_2);
    +posy(y_1);
    WHEN_END;
    WHEN_START;
    +posy(y_4);
    WHEN_BREAK;
    -posy(y_4);
    +posy(y_3);
    WHEN_END;
    WHEN_START;
    +posy(y_3);
    -posx(x_1);
    -posx(x_2);
    WHEN_BREAK;
    -posy(y_3);
    +posy(y_2);
    WHEN_END.


@action2 +!move_down <-
    WHEN_START;
    +posy(y_0);
    WHEN_BREAK;
    -posy(y_0);
    +posy(y_1);
    WHEN_END;
    WHEN_START;
    +posy(y_2);
    WHEN_BREAK;
    -posy(y_2);
    +posy(y_3);
    WHEN_END;
    WHEN_START;
    +posy(y_3);
    WHEN_BREAK;
    -posy(y_3);
    +posy(y_4);
    WHEN_END;
    WHEN_START;
    +posy(y_1);
    -posx(x_1);
    -posx(x_2);
    WHEN_BREAK;
    -posy(y_1);
    +posy(y_2);
    WHEN_END.


@action3 +!move_right <-
    WHEN_START;
    +posx(x_2);
    WHEN_BREAK;
    -posx(x_2);
    +posx(x_3);
    WHEN_END;
    WHEN_START;
    +posx(x_3);
    WHEN_BREAK;
    -posx(x_3);
    +posx(x_4);
    WHEN_END;
    WHEN_START;
    +posx(x_0);
    -posy(y_2);
    WHEN_BREAK;
    -posx(x_0);
    +posx(x_1);
    WHEN_END;
    WHEN_START;
    +posx(x_1);
    -posy(y_2);
    WHEN_BREAK;
    -posx(x_1);
    +posx(x_2);
    WHEN_END.

@action4 +!move_left <-
    WHEN_START;
    +posx(x_4);
    WHEN_BREAK;
    -posx(x_4);
    +posx(x_3);
    WHEN_END;
    WHEN_START;
    +posx(x_1);
    WHEN_BREAK;
    -posx(x_1);
    +posx(x_0);
    WHEN_END;
    WHEN_START;
    +posx(x_3);
    -posy(y_2);
    WHEN_BREAK;
    -posx(x_3);
    +posx(x_2);
    WHEN_END;
    WHEN_START;
    +posx(x_2);
    -posy(y_2);
    WHEN_BREAK;
    -posx(x_2);
    +posx(x_1);
    WHEN_END.