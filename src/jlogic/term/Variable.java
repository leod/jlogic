package jlogic.term;

public class Variable implements Term {
    private final String name;

    public Variable(String name) {
        if (name == null)
            throw new IllegalArgumentException("name must not be null");
        if (!isValidName(name))
            throw new IllegalArgumentException("name is invalid");

        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;

        Variable other = (Variable) object;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getName() {
        return name;
    }

    private static boolean isValidName(String name) {
        // TODO: Implement isValidName.
        return !name.isEmpty()
                && (name.charAt(0) == '_'
                || Character.isUpperCase(name.charAt(0)));
    }
}
