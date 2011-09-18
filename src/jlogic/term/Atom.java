package jlogic.term;

public final class Atom implements Term {
    private final String name;

    public Atom(String name) {
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

        Atom other = (Atom) object;
        return name.equals(other.name);
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    private static boolean isValidName(String name) {
        // TODO: Implement isValidName.
        return true;
    }
}
