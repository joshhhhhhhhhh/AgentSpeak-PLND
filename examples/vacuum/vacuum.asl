//IN ENVIRONMENT SETUP
//
// object(cell, n) - for each cell  (0, 1, 2 ...)
// dirty(n) - for each cell (0, 1, 2 ...)
// pos(c0)
// desires([clean(c0), clean(c1), ... , clean(cn)])


//initial beliefs - Initialized in the environment

!des.
+!des : desires(Goals) <-
    .print("Goals to plan: ", Goals);
    org.soton.peleus.act.plan(Goals, [makeGeneric(false)]);
    +start(system.time).

@action1[type(X, cell, temp)] +!suck1(X) : pos(X) & dirty(X) & linked(c3, X) <-
    -dirty(X);
    +clean(X);
    ONEOF_START;
    -dirty(c48);
    +clean(c48);
    ONEOF_BREAK;
    ONEOF_END.

@action2[type(X, cell, temp)] +!suck2(X) : pos(X) & dirty(X) & linked(X, c1) <-
    -dirty(X);
    +clean(X);
    ONEOF_START;
    -dirty(c1);
    +clean(c1);
    ONEOF_BREAK;
    ONEOF_END.

@action3[type(X, cell, temp), type(R, cell, temp), type(L, cell, temp)] +!suck3(X, L, R) : pos(X) & dirty(X) & linked(L, X) & linked(X, R) <-
    -dirty(X);
    +clean(X);
    ONEOF_START;
    -dirty(L);
    +clean(L);
    -dirty(R);
    +clean(R);
    ONEOF_BREAK;
    ONEOF_END.

@action4[type(X, cell, temp)] +!suck4(X) : pos(X) & clean(X) <-
    ONEOF_START;
    +dirty(X);
    -clean(X);
    ONEOF_BREAK;
    ONEOF_END.

@action5[type(X, cell, temp), type(R, cell, temp)] +!right(X, R) : pos(X) & linked(X, R) <-
    +pos(R);
    -pos(X).

@action6[type(X, cell, temp), type(L, cell, temp)] +!left(X, L) : pos(X) & linked(L, X) <-
    +pos(L);
    -pos(X).

-!act : start(X) <-
    .print("TIME TAKEN: ", system.time - X).



