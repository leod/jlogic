package jlogic.read;

import java.util.ArrayList;

import jlogic.term.*;

public final class Parser {
    private final Lexer lexer;

    private Token current;

    public Parser(Lexer lexer) throws ReadException {
        this.lexer = lexer;

        current = lexer.read();
    }

    public Term parseTerm() throws ReadException {
        switch (current.getType()) {
            case Identifier:
                if (Character.isUpperCase(current.getString().charAt(0))) {
                    // TODO: Check for valid variable name
                    Term result = new Variable(current.getString());
                    advance();
                    return result;
                } else {
                    Atom atom = parseAtom();

                    if (current.getType() == TokenType.LeftParen) {
                        advance();

                        ArrayList<Term> arguments = new ArrayList<Term>();

                        while (current.getType() != TokenType.RightParen) {
                            arguments.add(parseTerm());
                            if (current.getType() == TokenType.Comma)
                                advance();
                            // TODO: This allows trailing commas
                        }

                        advance();

                        Term[] argumentArray = new Term[arguments.size()];
                        return new Structure(atom, arguments.toArray(argumentArray));
                    } else {
                        return atom;
                    }
                }
            case Underscore:
                return new AnonymousVariable();
            default:
                throw new ReadException(current.getLocation(), "Expected term");
        }
    }

    public Atom parseAtom() throws ReadException {
        String name = current.getString();
        checkAdvance(TokenType.Identifier);
        // TODO: Check for valid atom name

        return new Atom(name);
    }

    private void advance() throws ReadException {
        current = lexer.read();
    }

    private void checkAdvance(TokenType currentType) throws ReadException {
        if (current.getType() != currentType)
            throw new ReadException(current.getLocation(), "Expected " + currentType.toString() + ", got " + current.getType().toString());
        advance();
    }

    private void advanceExpect(TokenType expectedType) throws ReadException {
        advance();
        if (current.getType() != expectedType)
            throw new ReadException(current.getLocation(), "Expected " + expectedType.toString() + ", got " + current.getType().toString());
    }
}
