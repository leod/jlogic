package jlogic;

import jlogic.term.*;
import jlogic.read.*;

public final class Main {
    public static void main(String[] args) throws ReadException {
        Term left = termFromString("numeral(succ(X))");
        Term right = termFromString("numeral(succ(succ(succ(X))))");

        System.out.println(left);
        System.out.println(right);

        Frame frame = new Frame();
        System.out.println(Matcher.match(frame, left, right));
    }

    private static Term termFromString(String string) throws ReadException {
        final String file = "[main" + string.hashCode() + "]";
        Lexer lexer = new Lexer(file, string);

        /*
         * Token token; do { token = lexer.read(); System.out.println(token); }
         * while (token.getType() != TokenType.EndOfFile);
         * 
         * lexer = new Lexer(file, string);
         */

        Parser parser = new Parser(lexer);

        return parser.parseTerm();
    }
}
