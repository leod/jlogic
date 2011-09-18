package jlogic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import jlogic.interpret.Frame;
import jlogic.interpret.Matcher;
import jlogic.read.Lexer;
import jlogic.read.Parser;
import jlogic.read.ReadException;
import jlogic.term.Term;

public final class Main {
    public static void main(String[] args) throws ReadException, IOException {
        Knowledge knowledge = readFile("test.jl");
        System.out.println(knowledge);

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

    private static Knowledge readFile(String path) throws ReadException, IOException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream("test.jl");
            Lexer lexer = new Lexer(path, new InputStreamReader(stream));
            Parser parser = new Parser(lexer);
            return parser.parseKnowledge();
        } finally {
            if (stream != null)
                stream.close();
        }
    }
}
