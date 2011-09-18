package jlogic.interpret;

import java.util.HashMap;

import jlogic.term.*;
import jlogic.term.*;

public final class Frame {
    private HashMap<Variable, Term> instantiations = new HashMap<Variable, Term>();

    // Suppress warnings for the cast after clone()
    @SuppressWarnings("unchecked")
    private HashMap<Variable, Term> cloneInstantiations() {
        return (HashMap<Variable, Term>) this.instantiations.clone();
    }

    public Frame() {

    }

    public Frame(Frame other) {
        instantiations = other.cloneInstantiations();
    }

    public void instantiate(Variable variable, Term term) {
        instantiations.put(variable, term);
    }

    public boolean hasInstantiation(Variable variable) {
        return instantiations.containsKey(variable);
    }

    public Term getInstantiation(Variable variable) {
        return instantiations.get(variable);
    }

    public String toString() {
        return instantiations.toString();
    }
}
