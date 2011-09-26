package jlogic.interpret;

import jlogic.term.AnonymousVariable;
import jlogic.term.Atom;
import jlogic.term.Structure;
import jlogic.term.Term;
import jlogic.term.Variable;

public final class Match {
    // Static class
    private Match() {
        assert false;
    }

    /**
     * Try to match two terms, instantiating variables as needed.
     * 
     * @param frame
     *        A frame containing pre-existing instantiations. This frame is not
     *        modified.
     * @param a
     * @param b
     * @return Either a new frame or null, if the match was unsuccessful.
     */
    public static Frame match(Frame frame, Term a, Term b) {
        // System.out.println("Match " + a + " and " + b + " in frame " +
        // frame);

        if (a.equals(b))
            return frame;

        // Emulate double dispatch
        if (a instanceof Atom && b instanceof Atom)
            return matchAtoms(frame, (Atom) a, (Atom) b);
        if (a instanceof Structure && b instanceof Structure)
            return matchStructures(frame, (Structure) a, (Structure) b);
        if (a instanceof Variable)
            return matchVariable(frame, (Variable) a, b);
        if (b instanceof Variable)
            return matchVariable(frame, (Variable) b, a);
        if (a instanceof AnonymousVariable)
            return frame;
        if (b instanceof AnonymousVariable)
            return frame;

        return null;
    }

    private static Frame matchAtoms(Frame frame, Atom a, Atom b) {
        return a.equals(b) ? frame : null;
    }

    private static Frame matchVariable(Frame frame, Variable variable, Term term) {
        Term instantiation = frame.getInstantiation(variable);
        if (instantiation != null)
            return match(frame, instantiation, term);

        Frame newFrame = new Frame(frame);
        newFrame.instantiate(variable, term);
        return newFrame;
    }

    private static Frame matchStructures(Frame frame, Structure a, Structure b) {
        if (a.getArity() != b.getArity())
            return null;
        if (!a.getFunctor().equals(b.getFunctor()))
            return null;

        Term[] argumentsA = a.getArguments();
        Term[] argumentsB = b.getArguments();

        assert argumentsA.length == argumentsB.length;

        Frame newFrame = new Frame(frame);
        for (int i = 0; i < argumentsA.length; ++i) {
            newFrame = match(newFrame, argumentsA[i], argumentsB[i]);
            if (newFrame == null)
                return null;
        }

        return newFrame;
    }
}
