package jlogic;

public final class Structure implements Term {
    private final Atom functor;
    private final Term[] arguments;

    public Structure(Atom functor, Term[] arguments) {
        this.functor = functor;
        this.arguments = arguments;
    }
    
    public Atom getFunctor() {
        return functor;
    }
    
    public Term[] getArguments() {
        return arguments;
    }
}
