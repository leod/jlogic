package jlogic.read;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public final class Lexer {
    private String file;
    private int line = 0;
    private int column = 0;

    private Reader code;
    private char current;
    private boolean endOfFile = false;

    private static final HashMap<Character, TokenType> simpleTokenTypes = new HashMap<Character, TokenType>();

    static {
        simpleTokenTypes.put('(', TokenType.LeftParen);
        simpleTokenTypes.put(')', TokenType.RightParen);
        simpleTokenTypes.put('_', TokenType.Underscore);
        simpleTokenTypes.put(',', TokenType.Comma);
        simpleTokenTypes.put(':', TokenType.Colon);
        simpleTokenTypes.put('-', TokenType.Hyphen);
        simpleTokenTypes.put('.', TokenType.Period);
    }

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

        skipWhitespace();

        while (current == '%')
            skipComment();

        if (Character.isLetter(current))
            return readIdentifier();

        TokenType type = simpleTokenTypes.get(current);
        if (type != null) {
            Token token = new Token(createLocation(), type, Character.toString(current));
            advance();
            return token;
        }

        throw new ReadException(createLocation(), "Unknown character: " + current);
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

    private void skipComment() throws IOException {
        assert current == '%';
        while (!endOfFile && current != '\n') {
            advance();
        }
        skipWhitespace();
    }
}
