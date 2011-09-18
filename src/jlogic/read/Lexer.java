package jlogic.read;

import java.io.Reader;
import java.io.IOException;

public final class Lexer {
    private String file;
    private int line = 0;
    private int column = 0;

    private Reader code;
    private char current;
    private boolean endOfFile = false;

    public Lexer(String file, Reader code) throws IOException {
        this.file = file;
        this.code = code;

        int input = code.read();
        if (input == -1)
            endOfFile = true;
        current = (char) input;
    }

    public String getFile() {
        return file;
    }

    public Token read() throws ReadException, IOException {
        if (endOfFile)
            return new Token(createLocation(), TokenType.EndOfFile, "");

        Token token = null;
        if (Character.isLetter(current)) {
            token = readIdentifier();
        } else if (current == '(') {
            token = new Token(createLocation(), TokenType.LeftParen, "(");
            advance();
        } else if (current == ')') {
            token = new Token(createLocation(), TokenType.RightParen, ")");
            advance();
        } else if (current == '_') {
            token = new Token(createLocation(), TokenType.Underscore, ")");
            advance();
        } else if (current == ',') {
            token = new Token(createLocation(), TokenType.Comma, ",");
            advance();
        } else if (current == ':') {
            token = new Token(createLocation(), TokenType.Colon, ":");
            advance();
        } else if (current == '-') {
            token = new Token(createLocation(), TokenType.Hyphen, "-");
            advance();
        } else if (current == '.') {
            token = new Token(createLocation(), TokenType.Period, ".");
            advance();
        }

        if (token == null)
            throw new ReadException(createLocation(), "Unknown character: " + current);

        skipWhitespace();

        return token;
    }

    private void advance() throws IOException {
        ++column;

        int input = code.read();
        if (input == -1) {
            endOfFile = true;
            return;
        }

        current = (char) input;
    }

    private Location createLocation() {
        return new Location(file, line, column);
    }

    private Token readIdentifier() throws IOException {
        Location location = createLocation();

        StringBuilder stringBuilder = new StringBuilder();
        while (!endOfFile &&
                (Character.isLetter(current) || Character.isDigit(current) ||
                current == '_')) {
            stringBuilder.append(current);
            advance();
        }

        return new Token(location, TokenType.Identifier, stringBuilder.toString());
    }

    private void skipWhitespace() throws IOException {
        while (!endOfFile && Character.isWhitespace(current)) {
            if (current == '\n') {
                advance();
                column = 0;
                ++line;
            }
            else
                advance();
        }
    }
}
