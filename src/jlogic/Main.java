package jlogic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import jlogic.term.*;
import jlogic.interpret.*;
import jlogic.read.*;

public final class Main {
    public static void main(String[] args) throws ReadException, IOException {
        // String source =
        Term left = termFromString("a(X,X)");
        Term right = termFromString("a(Y,Y)");

        System.out.println(left);
        System.out.println(right);
        System.out.println("--------------");

        Frame frame = new Frame();
        System.out.println(Matcher.match(frame, left, right));
    }

    private static Term termFromString(String string) throws ReadException, IOException {
        final String file = "[main" + string.hashCode() + "]";
        Lexer lexer = new Lexer(file, new StringReader(string));

        /*
         * Token token; do { token = lexer.read(); System.out.println(token); }
         * while (token.getType() != TokenType.EndOfFile);
         * 
         * lexer = new Lexer(file, string);
         */

        Parser parser = new Parser(lexer);

        return parser.parseTerm();
    }

    private static String readFile(String path) throws IOException {
        // FileInputStream stream = new FileInputStream(new File(path));
        return null;
    }
}
