package jlogic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import jlogic.read.Lexer;
import jlogic.read.Parser;
import jlogic.read.ReadException;

public final class Main {
    public static void main(String[] args) throws ReadException, IOException, InterruptedException {
        Knowledge knowledge = readFile("test.jl");
        REPL repl = new REPL(knowledge, System.in, System.out);
        repl.run();
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
