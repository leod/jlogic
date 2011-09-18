package jlogic;

public final class Main {
    public static void main(String[] args) {
        Frame frame = new Frame();

        Term left;
        Term right;

        left = new Structure(new Atom("a"), new Term[] { new Atom("b"), new Variable("X") });
        right = new Structure(new Atom("a"), new Term[] { new Atom("b"), new Structure(new Atom("c"), new Term[] { new Variable("Y"), new Atom("e") }) });

        System.out.println(Matcher.match(frame, left, right));
    }
}
