package jlogic.term;

public interface Term {
    // Visitor pattern
    public <T> T accept(Visitor<T> visitor);
}
