package jlogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import jlogic.interpret.DOT;
import jlogic.interpret.Frame;
import jlogic.interpret.SearchTree;
import jlogic.read.Lexer;
import jlogic.read.Parser;
import jlogic.read.ReadException;
import jlogic.term.Structure;
import jlogic.term.Term;
import jlogic.term.Variable;

public final class REPL {
    private final String helpString = "" +
            "Input a query in the form of a valid term (for example foo(Bar), or bla(blub, X)).\n" +
            "The query will be evaluated against the knowledge base and valid solutions for the free variables in the query will be listed one by one.\n" +
            "Additionally, special commands prefixed by a colon can be issued.\n\n" +
            "Special commands:\n" +
            "\t:quit\t\t\tStop the REPL.\n" +
            "\t:help\t\t\tPrint this help.\n" +
            "\t:consult file\t\tLoad rules and facts from `file'.\n" +
            "\t:knowledge\t\tPrint all known rules and facts.\n" +
            "\t:savetree `file'\tCreate a PNG image named `file' containing the search tree of the last query.\n" +
            "\t\t\t\tIf no `file' is supplied, the default name `searchtree.png' is assumed.\n";

    private Knowledge knowledge;

    private final BufferedReader input;
    private final Writer output;

    private SearchTree lastSearchTree = null;
    private boolean quitLoop = false;

    public REPL(Knowledge knowledge, InputStream input, OutputStream output) {
        this.knowledge = knowledge;
        this.input = new BufferedReader(new InputStreamReader(input));
        this.output = new OutputStreamWriter(output);
    }

    public void run() throws IOException {
        while (!quitLoop) {
            Structure goal = read();
            if (goal == null)
                continue;
            evaluateAndPrint(goal);
        }
    }

    private void evaluateAndPrint(Structure goal) throws IOException {
        SearchTree searchTree = new SearchTree(knowledge, goal);

        Frame frame;
        String line = null;
        boolean writeAll = false;
        do {
            frame = searchTree.searchOne();
            writeInstantiations(frame);

            if (frame == null)
                break; // That was the last solution

            if (writeAll)
                continue;

            output.write("More? [y]/n/all: ");
            output.flush();
            line = input.readLine().trim();
            output.write("\n");

            if (line.equals("all"))
                writeAll = true;
        } while (writeAll
                || line.equals("") || line.equals("Y") || line.equals("y")
                || line.equals("yes") || line.equals(";"));

        output.write("\n");
        output.flush();

        lastSearchTree = searchTree;
    }

    // TODO: Read multiple goals at once
    private Structure read() throws IOException {
        while (true) {
            output.write("?- ");
            output.flush();

            String line = input.readLine().trim();

            if (line.equals("")) {
                // Don't create useless parser error messages on empty input
                return null;
            }

            if (line.endsWith("\n"))
                line = line.substring(0, line.length() - 1);

            if (line.startsWith(":")) {
                String command = line.substring(1);
                handleSpecialCommand(command);
                return null;
            }

            try {
                Lexer lexer = new Lexer("repl", new StringReader(line));
                Parser parser = new Parser(lexer);

                return parser.parseStructure();
            } catch (ReadException exception) {
                output.write("Syntax error: " + exception.getMessage() + ".");
                output.write("\n\n");
            }
        }
    }

    private void handleSpecialCommand(String command) throws IOException {
        String[] arguments = command.split("\\s+");

        if (arguments.length < 1) {
            output.write("No command name supplied.");
            output.flush();
            return;
        }

        if (arguments[0].equals("quit"))
            quitLoop = true;
        else if (arguments[0].equals("help"))
            output.write(helpString);
        else if (arguments[0].equals("consult"))
            output.write("Not implemented yet.");
        else if (arguments[0].equals("knowledge"))
            output.write(knowledge.toString());
        else if (arguments[0].startsWith("savetree")) {
            if (lastSearchTree != null) {
                String filename;
                if (arguments.length == 1)
                    filename = "searchtree.png";
                else
                    filename = arguments[1];

                DOT dot = new DOT(lastSearchTree);
                createGraphImage(filename, dot.generateDOT());

                output.write("Saved tree to " + filename);
            } else {
                output.write("No query was issued yet.");
            }
        } else
            output.write("Unknown command: " + command);

        output.write("\n");
        output.flush();
    }

    private void writeInstantiations(Frame frame) throws IOException {
        if (frame != null) {
            if (frame.getInstantiations().size() > 0) {
                for (Map.Entry<Variable, Term> entry : frame.getInstantiations().entrySet()) {
                    output.write(entry.getKey().toString());
                    output.write(" = ");
                    output.write(entry.getValue().toString());
                    output.write("\n");
                }
            } else
                output.write("Yes.\n");
        }
        else
            output.write("No.\n");
        output.flush();
    }

    private static void createGraphImage(String filename, String dot) throws IOException {
        // This might not always work, see
        // <http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4>.

        try {
            Process process = Runtime.getRuntime().exec("dot -o" + filename + " -Tpng");
            OutputStream stdin = process.getOutputStream();
            stdin.write(dot.getBytes());
            stdin.flush();
            stdin.close();
            process.waitFor();

            if (process.exitValue() != 0)
                throw new IOException("dot returned " + process.exitValue());
        } catch (InterruptedException exception) {
            // TODO: Need to find out when this happens and what to do
            throw new IOException(exception.toString());
        }
    }
}
