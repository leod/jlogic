package jlogic.term;

public final class AnonymousVariable implements Term {
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "_";
    }
}
