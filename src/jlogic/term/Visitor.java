package jlogic.term;

public interface Visitor<T> {
    T visit(AnonymousVariable anonymousVariable);

    T visit(Atom atom);

    T visit(Structure structure);

    T visit(Variable variable);
}
