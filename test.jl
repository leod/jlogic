numeral(zero).
 
numeral(succ(X)) :- numeral(X).

add(zero,Y,Y).
add(succ(X),Y,succ(Z)) :-
        add(X,Y,Z).