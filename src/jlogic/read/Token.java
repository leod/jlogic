package jlogic.read;

public final class Token {
    private final Location location;
    private final TokenType type;
    private final String string;

    public Token(Location location, TokenType type, String string) {
        this.location = location;
        this.type = type;
        this.string = string;
    }

    @Override
    public String toString() {
        return location.toString() + ": " + type.toString() + " " + string;
    }

    public Location getLocation() {
        return location;
    }

    public TokenType getType() {
        return type;
    }

    public String getString() {
        return string;
    }
}
