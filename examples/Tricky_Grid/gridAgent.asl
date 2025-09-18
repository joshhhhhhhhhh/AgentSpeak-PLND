


//range requires numbers, but they don't work with planners
//Solution: Include only numbers in jason, and when translating for a planner
// which uses epistemicjason add a a character in front of all of them

//semi_numeric:
//  Numeric in Jason
//  Non-Numeric to planners

//Define actions using non-numeric, but use numeric for everything else.
// These ranges will then be altered when being translated into planning problems.
// The planning problem outputs will be translated back into numbers.

//object(x_0, x)...
//TODO:
//  Implement Semi-Numerics
//  Add NDCPCES input/output window
//  test it!!
//  Create simple tricky grid env

//poss(test_case).
range(atx(X)) :- .range(X, 1, 3).
range(aty(X)) :- .range(X, 1, 3).

range(noneX) :- true.
noneX :- .findall(not(atx(X)), range(atx(X)), List) & .big_and(Y, List) & Y.
~noneX.

range(noneY) :- true.
noneY :- .findall(not(aty(X)), range(aty(X)), List) & .big_and(Y, List) & Y.
~noneY.

~atx(X1) :- atx(X2) & (X1 \== X2).
~aty(X1) :- aty(X2) & (X1 \== X2).


alive.
can_move.

!des.

+!des[source(self)] : desires(Goal) <-
    .print("STARTING PLANNER");
    org.soton.peleus.act.plan(Goal, [makeGeneric(false)]);
    .print("PLANNER COMPLETE").


@action1 +!check : has_to_check <-
    -has_to_check;
    +can_move;
    WHEN_START;
    +atx(x_0);
    +aty(y_4);
    WHEN_BREAK;
    -alive;
    WHEN_END;
    WHEN_START;
    +atx(x_1);
    +aty(y_4);
    WHEN_BREAK;
    -alive;
    WHEN_END;
    WHEN_START;
    +atx(x_2);
    +aty(y_4);
    WHEN_BREAK;
    -alive;
    WHEN_END;
    WHEN_START;
    +atx(x_3);
    +aty(y_4);
    WHEN_BREAK;
    -alive;
    WHEN_END;
    WHEN_START;
    +atx(x_4);
    +aty(y_4);
    WHEN_BREAK;
    -alive;
    WHEN_END;
    WHEN_START;
    +atx(x_0);
    +aty(y_0);
    WHEN_BREAK;
    -alive;
    WHEN_END;
    WHEN_START;
    +atx(x_4);
    +aty(y_0);
    WHEN_BREAK;
    -alive;
    WHEN_END.

@action2 +!move_up : can_move <-
    -can_move;
    +has_to_check;
    WHEN_START;
    +aty(y_0);
    WHEN_BREAK;
    -aty(y_0);
    +aty(y_1);
    WHEN_END;
    WHEN_START;
    +aty(y_1);
    WHEN_BREAK;
    -aty(y_1);
    +aty(y_2);
    WHEN_END;
    WHEN_START;
    +aty(y_2);
    WHEN_BREAK;
    -aty(y_2);
    +aty(y_3);
    WHEN_END;
    WHEN_START;
    +aty(y_3);
    WHEN_BREAK;
    -aty(y_3);
    +aty(y_4);
    WHEN_END.

@action3 +!move_down : can_move <-
    -can_move;
    +has_to_check;
    WHEN_START;
    +aty(y_4);
    WHEN_BREAK;
    -aty(y_4);
    +aty(y_3);
    WHEN_END;
    WHEN_START;
    +aty(y_3);
    WHEN_BREAK;
    -aty(y_3);
    +aty(y_2);
    WHEN_END;
    WHEN_START;
    +aty(y_2);
    WHEN_BREAK;
    -aty(y_2);
    +aty(y_1);
    WHEN_END;
    WHEN_START;
    +aty(y_1);
    WHEN_BREAK;
    -aty(y_1);
    +aty(y_0);
    WHEN_END.

@action4 +!move_left : can_move <-
    -can_move;
    +has_to_check;
    WHEN_START;
    +atx(x_4);
    WHEN_BREAK;
    -atx(x_4);
    +atx(x_3);
    WHEN_END;
    WHEN_START;
    +atx(x_3);
    WHEN_BREAK;
    -atx(x_3);
    +atx(x_2);
    WHEN_END;
    WHEN_START;
    +atx(x_2);
    WHEN_BREAK;
    -atx(x_2);
    +atx(x_1);
    WHEN_END;
    WHEN_START;
    +atx(x_1);
    WHEN_BREAK;
    -atx(x_1);
    +atx(x_0);
    WHEN_END.

@action5 +!move_right : can_move <-
    -can_move;
    +has_to_check;
    WHEN_START;
    +atx(x_0);
    WHEN_BREAK;
    -atx(x_0);
    +atx(x_1);
    WHEN_END;
    WHEN_START;
    +atx(x_1);
    WHEN_BREAK;
    -atx(x_1);
    +atx(x_2);
    WHEN_END;
    WHEN_START;
    +atx(x_2);
    WHEN_BREAK;
    -atx(x_2);
    +atx(x_3);
    WHEN_END;
    WHEN_START;
    +atx(x_3);
    WHEN_BREAK;
    -atx(x_3);
    +atx(x_4);
    WHEN_END;
    WHEN_START;
    +aty(y_0);
    WHEN_BREAK;
    ONEOF_START;
    +aty(y_0);
    ONEOF_BREAK;
    -aty(y_0);
    +aty(y_1);
    ONEOF_END;
    WHEN_END;
    WHEN_START;
    +aty(y_1);
    WHEN_BREAK;
    ONEOF_START;
    +aty(y_1);
    ONEOF_BREAK;
    -aty(y_1);
    +aty(y_2);
    ONEOF_END;
    WHEN_END;
    WHEN_START;
    +aty(y_2);
    WHEN_BREAK;
    ONEOF_START;
    +aty(y_2);
    ONEOF_BREAK;
    -aty(y_2);
    +aty(y_3);
    ONEOF_END;
    WHEN_END;
    WHEN_START;
    +aty(y_3);
    WHEN_BREAK;
    ONEOF_START;
    +aty(y_3);
    ONEOF_BREAK;
    -aty(y_3);
    +aty(y_4);
    ONEOF_END;
    WHEN_END.
