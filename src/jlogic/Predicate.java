package jlogic;

public final class Predicate {
    private final Rule[] clauses;
    private final String name;
    private final String fullName;
    private final int arity;

    public Predicate(Rule[] clauses) {
        if (clauses == null || clauses.length == 0)
            throw new IllegalArgumentException("must supply at least one clause per predicate");
        if (!isUniformFullName(clauses))
            throw new IllegalArgumentException("every clause in a predicate must have the same arity");

        this.clauses = clauses;

        name = clauses[0].getHead().getFunctor().getName();
        fullName = clauses[0].getHead().getFullName();
        arity = clauses[0].getHead().getArity();
    }

    public String getName() {
        return name;
    }

    public Rule[] getClauses() {
        return clauses;
    }

    public int getArity() {
        return arity;
    }

    public String getFullName() {
        return fullName;
    }

    private static boolean isUniformFullName(Rule[] clauses) {
        String fullName = clauses[0].getHead().getFullName();

        for (int i = 1; i < clauses.length; ++i) {
            if (!clauses[i].getHead().getFullName().equals(fullName))
                return false;
        }

        return true;
    }
}
