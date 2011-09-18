package jlogic.read;

public final class Location {
    private final String file;
    private final int line;
    private final int column;

    public Location(String file, int line, int column) {
        this.file = file;
        this.line = line;
        this.column = column;
    }

    public String toString() {
        return line + "," + column + "@" + file;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
