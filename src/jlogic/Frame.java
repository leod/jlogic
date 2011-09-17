package jlogic;

import java.util.HashMap;

public final class Frame {
    private HashMap<Variable, Term> instantiations = new HashMap<Variable, Term>();

    // Suppress warnings for the cast after clone()
    @SuppressWarnings("unchecked")
    public Frame clone() {
        HashMap<Variable, Term> instantiations = (HashMap<Variable, Term>)
                this.instantiations.clone();

        Frame result = new Frame();
        result.instantiations = instantiations;

        return result;
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
