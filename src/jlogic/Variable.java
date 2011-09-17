package jlogic;

public class Variable implements Term {
    private final String name;

    public Variable(String name) {
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
        return !name.isEmpty()
                && (name.charAt(0) == '_' || Character.isUpperCase(name
                        .charAt(0)));
    }
}
