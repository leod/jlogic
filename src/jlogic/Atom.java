package jlogic;

public final class Atom implements Term {
    private String name;

    public Atom(String name) {
        if (name == null)
            throw new IllegalArgumentException("name must not be null");

        this.name = name;
    }

    public String getName() {
        return name;
    }
}
