package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Created by Dominik on 3.10.2016..
 */
public class PositionParser {
    enum Position {
        TOP("top"), LEFT("left"), RIGHT("right"), BOTTOM("bottom");

        private String string;

        Position(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }

        public static Optional<Position> getValueFromString(String string) {
            return Arrays.stream(Position.values()).filter(p -> p.string.equals(string))
                    .findFirst();
        }
    }

    private Lexer lexer;
    private LinkedHashSet<Definition> definitions = new LinkedHashSet<>();

    public PositionParser(String fileName) throws IOException {
        String data = new String(Files.readAllBytes(Paths.get("testovi/" + fileName + ".sim")),
                StandardCharsets.UTF_8);
        lexer = new Lexer(data);
        parse();
    }

    private void parse() {
        while (lexer.getCurrentToken().getType() != TokenType.EOF) {
            parseLine();
        }
    }

    private void parseLine() {
        checkType(TokenType.IDENT);
        String signal = (String) currentValue();
        Definition definition = new Definition(signal);
        lexer.nextToken();

        if (lexer.getCurrentToken().getType() == TokenType.OPEN_PARENTHESES) {
            lexer.nextToken();
            parseParentheses(definition);
        }

        Optional<Position> position = Position.getValueFromString((String) currentValue());
        if (position.isPresent()) {
            definition.position = position.get();
        } else {
            throw new ParserException("Invalid position for signal: " + signal + ".");
        }
        lexer.nextToken();

        if (definition.getType() == Definition.Type.VECTOR) {
            decomposeDefinition(definition);
        } else {
            if (!definitions.add(definition)) {
                throw new ParserException("Error adding signal: " + definition.signal);
            }
        }
    }

    private void decomposeDefinition(Definition definition) {
        int step;
        if (definition.start < definition.end) {
            step = 1;
        } else {
            step = -1;
        }

        int index = definition.start;
        while (true) {
            Definition def = new Definition(definition.signal);
            def.position = definition.position;
            def.access = index;
            if (!definitions.add(def)) {
                throw new ParserException("Error adding signal: " + def.signal);
            }

            if (index == definition.end) {
                break;
            }
            index += step;
        }
    }

    private void parseParentheses(Definition definition) {
        checkType(TokenType.NUMBER);
        int number = (int) currentValue();
        lexer.nextToken();

        if (lexer.getCurrentToken().getType() == TokenType.CLOSED_PARENTHESES) {
            definition.access = number;
            lexer.nextToken();
        } else if (lexer.getCurrentToken().getType() == TokenType.KEYWORD) {
            Vector.Order order = getOrder(definition, (String) currentValue());
            lexer.nextToken();

            checkType(TokenType.NUMBER);
            int second = (int) currentValue();
            checkOrder(definition.signal, number, order, second);
            definition.start = number;
            definition.end = second;
            lexer.nextToken();

            checkType(TokenType.CLOSED_PARENTHESES);
            lexer.nextToken();
        } else {
            throw new ParserException("Invalid line for signal " + definition.signal + ".");
        }
    }

    private void checkOrder(String signal, int first, Vector.Order order, int second) {
        if (order == Vector.Order.TO && first <= second) {
            return;
        }
        if (order == Vector.Order.DOWNTO && first >= second) {
            return;
        }

        throw new ParserException("Invalid order for signal " + signal + ".");
    }

    private Vector.Order getOrder(Definition definition, String order) {
        switch (order) {
            case "to":
                return Vector.Order.TO;
            case "downto":
                return Vector.Order.DOWNTO;
            default:
                throw new ParserException(
                        "Invalid order for signal " + definition.signal + ".");
        }
    }

    private Object currentValue() {
        return lexer.getCurrentToken().getValue();
    }

    private void checkType(TokenType type) {
        if (lexer.getCurrentToken().getType() != type) {
            throw new ParserException("Expect identification in sim file");
        }
    }

    public static class Definition {
        enum Type {
            SINGLE, VECTOR, ELEMENT;
        }

        private String signal;
        private Position position;

        private Integer access;

        private Integer start;
        private Integer end;

        public Definition(String signal) {
            this.signal = signal;
        }

        public Type getType() {
            if (access != null) {
                return Type.ELEMENT;
            } else if (start != null) {
                return Type.VECTOR;
            } else {
                return Type.SINGLE;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Definition that = (Definition) o;

            if (!signal.equals(that.signal)) return false;
            return access != null ? access.equals(that.access) : that.access == null;

        }

        @Override
        public int hashCode() {
            int result = signal.hashCode();
            result = 31 * result + (access != null ? access.hashCode() : 0);
            return result;
        }
    }

    public static void main(String[] args) throws IOException {
        PositionParser pp = new PositionParser("testPozicije");
        pp.definitions.forEach(d -> System.out.println(d.signal + "(" + d.access + ") " + d
                .position));
    }

}
