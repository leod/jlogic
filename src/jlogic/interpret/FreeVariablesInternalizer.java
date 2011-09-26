package jlogic.interpret;

import jlogic.term.AnonymousVariable;
import jlogic.term.Atom;
import jlogic.term.Structure;
import jlogic.term.Term;
import jlogic.term.Variable;
import jlogic.term.Visitor;

import fj.F;
import fj.data.List;

/**
 * This class searches for free variables (e.g. X in foo(X)) in a term and
 * instantiates them by internal variables (e.g. foo(_G1)).
 */
public final class FreeVariablesInternalizer implements Visitor<Term> {
    // TODO: Code duplication with Instantiator

    private final InternalVariableFactory internalVariableFactory;
    private Frame frame;

    public FreeVariablesInternalizer(
            InternalVariableFactory internalVariableFactory,
            Frame frame) {
        this.internalVariableFactory = internalVariableFactory;
        this.frame = frame;
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
        if (!variable.getName().startsWith("_G") /* HACK! */) {
            Term instantiation = frame.getInstantiation(variable);
            if (instantiation == null) {
                Variable internalVariable = internalVariableFactory.create();
                frame.instantiate(variable, internalVariable);
                return internalVariable;
            }
            return instantiation;
        }
        return variable;
    }

    public List<Term> visit(List<Term> terms) {
        return terms.map(new F<Term, Term>() {
            @Override
            public Term f(Term term) {
                return term.accept(FreeVariablesInternalizer.this);
            }
        });
    }
}
