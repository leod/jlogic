append(nil, R, R). 
append(cons(Head, TailA), B, cons(Head, TailR)) :- append(TailA, B, TailR).

numeral(zero).
numeral(succ(X)) :- numeral(X).

add(zero, R, R).
add(succ(A), B, succ(R)) :- add(A, B, R).

sub(R, zero, R).
sub(succ(A), succ(B), R) :- sub(A, B, R).

sum(nil, zero).
sum(cons(Head, Tail), R) :- sub(R, Head, RPrime), sum(Tail, RPrime).