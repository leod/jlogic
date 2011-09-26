% Basic stuff
equals(X, X).

% Lists
append(nil, R, R). 
append(cons(Head, TailA), B, cons(Head, TailR)) :- append(TailA, B, TailR).

member(X, cons(X, _)).
member(X, cons(_, T)) :- member(X, T).

% Numbers
numeral(zero).
numeral(succ(X)) :- numeral(X).

add(zero, R, R).
add(succ(A), B, succ(R)) :- add(A, B, R).

sub(R, zero, R).
sub(succ(A), succ(B), R) :- sub(A, B, R).

sum(nil, zero).
sum(cons(Head, Tail), R) :- sub(R, Head, RPrime), sum(Tail, RPrime).

% Graph generation test
p(a).                              
p(X) :- q(X), r(X).                
p(X) :- u(X).                      
 
q(X) :- s(X).                      


r(a).                              
r(b). 


s(a).
s(b).
s(c).
 
u(d).

% Trees
swap(leaf(A), leaf(A)).
swap(tree(A, B), tree(C, D)) :- swap(A, D), swap(B, C).

% Grammar test
sentence(Z) :- np(X), vp(Y), append(X,Y,Z).
 
np(Z) :- det(X), n(Y), append(X,Y,Z).
 
vp(Z) :-  v(X), np(Y), append(X,Y,Z).
 
vp(Z) :-  v(Z).
 
det(cons(the, nil)).
det(cons(a, nil)).
 
n(cons(woman, nil)).
n(cons(man, nil)).
 
v(cons(shoots, nil)).