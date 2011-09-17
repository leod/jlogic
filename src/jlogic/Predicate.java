package jlogic;

public final class Predicate {
    private final String name;
    private final Rule[] clauses;
    private final int arity;
    private final String fullName;

    public Predicate(String name, Rule[] clauses) {
        if (clauses == null || clauses.length == 0)
            throw new IllegalArgumentException("must supply at least one clause per predicate");
        if (!isUniformArity(clauses))
            throw new IllegalArgumentException("every clause in a predicate must have the same arity");
        if (name == null)
            throw new IllegalArgumentException("name must not be null");

        this.name = name;
        this.clauses = clauses;

        arity = clauses[0].getHead().getArity();
        fullName = name + '/' + Integer.toString(arity);
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

    private static boolean isUniformArity(Rule[] clauses) {
        int arity = clauses[0].getHead().getArity();

        for (int i = 1; i < clauses.length; ++i) {
            if (clauses[i].getHead().getArity() != arity)
                return false;
        }

        return true;
    }
}
