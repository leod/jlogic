package jlogic.interpret;

import jlogic.term.Variable;

// Every Java project needs at least one factory
public final class InternalVariableFactory {
    private int counter = 0;

    public Variable create() {
        // TODO: Subclass Variable for internal variables?
        return new Variable("_G" + counter++);
    }
}
