package jlogic.term;

public final class Structure implements Term {
    private final Atom functor;
    private final Term[] arguments;

    public Structure(Atom functor, Term[] arguments) {
        if (functor == null)
            throw new IllegalArgumentException("functor must not be null");
        if (arguments == null)
            throw new IllegalArgumentException("arguments must not be null");

        this.functor = functor;
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;

        Structure other = (Structure) object;
        if (!other.functor.equals(object))
            return false;
        if (other.arguments.length != arguments.length)
            return false;

        for (int i = 0; i < other.arguments.length; ++i) {
            if (!other.arguments[i].equals(arguments[i]))
                return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(functor);
        builder.append('(');

        for (int i = 0; i < arguments.length; ++i) {
            builder.append(arguments[i]);

            if (i + 1 != arguments.length)
                builder.append(',');
        }

        builder.append(')');

        return builder.toString();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Atom getFunctor() {
        return functor;
    }

    public Term[] getArguments() {
        return arguments;
    }

    public int getArity() {
        return arguments.length;
    }

    public String getName() {
        return functor.getName();
    }

    public String getFullName() {
        return getName() + '/' + getArity();
    }
}
