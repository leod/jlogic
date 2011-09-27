package jlogic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

import jlogic.read.Lexer;
import jlogic.read.Parser;
import jlogic.read.ReadException;
import jlogic.term.Term;

public final class Main {
    public static void main(String[] args) throws ReadException, IOException, InterruptedException {
        Knowledge knowledge = readFile("test.jl");
        REPL repl = new REPL(knowledge, System.in, System.out, System.err);
        repl.run();

        // DOT dot = new DOT(search);
        // createGraphImage("searchgraph.png", dot.generateDOT());
    }

    private static Term termFromString(String string) throws ReadException, IOException {
        final String file = "[main" + string.hashCode() + "]";
        Lexer lexer = new Lexer(file, new StringReader(string));
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

    private static void createGraphImage(String filename, String dot) throws IOException, InterruptedException {
        // This might not always work, see
        // <http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4>.

        Process process = Runtime.getRuntime().exec("dot -o" + filename + " -Tpng");
        OutputStream stdin = process.getOutputStream();
        stdin.write(dot.getBytes());
        stdin.flush();
        stdin.close();
        process.waitFor();

        if (process.exitValue() != 0)
            throw new IOException("dot returned " + process.exitValue());
    }
}
