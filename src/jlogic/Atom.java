package jlogic;

public final class Atom implements Term {
    private final String name;

    public Atom(String name) {
        if (name == null)
            throw new IllegalArgumentException("name must not be null");
        if (!isValidName(name))
            throw new IllegalArgumentException("name is invalid");

        this.name = name;
    }

    public String getName() {
        return name;
    }

    private static boolean isValidName(String name) {
        // TODO: Implement isValidName.
        return true;
    }
}
