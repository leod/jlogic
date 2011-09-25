package jlogic.interpret;

import jlogic.term.AnonymousVariable;
import jlogic.term.Atom;
import jlogic.term.Structure;
import jlogic.term.Term;
import jlogic.term.Variable;
import jlogic.term.Visitor;
import fj.F;
import fj.data.List;

public final class Instantiator implements Visitor<Term> {
    private Frame instantiations;

    public Instantiator(Frame instantiations) {
        this.instantiations = instantiations;
    }

    @Override
    public Term visit(AnonymousVariable anonymousVariable) {
        return anonymousVariable;
    }

    @Override
    public Term visit(Atom atom) {
        return atom;
    }

    @Override
    public Term visit(Structure structure) {
        Term[] newArguments = new Term[structure.getArity()];
        for (int i = 0; i < structure.getArity(); ++i) {
            newArguments[i] = structure.getArguments()[i].accept(this);
        }

        return new Structure(structure.getFunctor(), newArguments);
    }

    @Override
    public Term visit(Variable variable) {
        Term instantiation = instantiations.getInstantiation(variable);
        if (instantiation != null)
            return instantiation;
        return variable;
    }

    public List<Term> visit(List<Term> terms) {
        return terms.map(new F<Term, Term>() {
            @Override
            public Term f(Term term) {
                return term.accept(Instantiator.this);
            }
        });
    }
}
