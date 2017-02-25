package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.Token;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortType;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Type;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.models.values.VectorOrder;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Constant;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.OperatorFactory;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.DeclarationExpression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary.IndexerOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mappable;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Created by Dominik on 25.7.2016..
 */
public class Parser {
    private Lexer lexer;
    private ProgramNode programNode;
    private DeclarationTable table = new DeclarationTable();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        programNode = parse();
    }

    public DeclarationTable getTable() {
        return table;
    }

    public ProgramNode getProgramNode() {
        return programNode;
    }

    private boolean isTokenOfType(TokenType type) {
        return lexer.getCurrentToken().getType() == type;
    }

    private void checkType(TokenType type, String message) {
        checkType(type, null, false, message);
    }

    private void checkType(TokenType type, Object value, String message) {
        checkType(type, value, true, message);
    }

    private void checkType(TokenType type, Object value, boolean checkValue, String message) {
        if (lexer.getCurrentToken().getType() != type) {
            throw new ParserException(message);
        }
        if (checkValue && !lexer.getCurrentToken().getValue().equals(value)) {
            throw new ParserException("Expected: " + value + ". Got: " + lexer.getCurrentToken().getValue());
        }
    }

    private Object currentValue() {
        return lexer.getCurrentToken().getValue();
    }

    private ProgramNode parse() {
        checkType(TokenType.KEYWORD, "entity", "Expected entity block.");
        lexer.nextToken();
        EntityNode entity = parseEntity();

        checkType(TokenType.KEYWORD, "architecture", "Expected ARCHITECTURE block.");
        lexer.nextToken();
        ArchitectureNode arch = parseArchitecture();

        checkType(TokenType.EOF, "End of file expected.");

        return new ProgramNode(entity, arch, table);
    }

    private ArchitectureNode parseArchitecture() {
        checkType(TokenType.IDENT, "Expected identification.");
        String name = (String) currentValue();
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "of", "Expected keyword OF");
        lexer.nextToken();

        checkType(TokenType.IDENT, "Expected identification.");
        if (!table.getEntryName().equals(currentValue())) {
            throw new ParserException("Component name in the architecture does not match entity " + "name.");
        }
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "is", "Expected keyword IS");
        lexer.nextToken();

        ArchitectureNode node = new ArchitectureNode(name);
        table.setArchName(name);

        while (isTokenOfType(TokenType.KEYWORD) && "signal".equals(currentValue())) {
            lexer.nextToken();

            List<Declaration> signals = parseInternalSignals();
            node.addSignals(signals);
        }

        checkType(TokenType.KEYWORD, "begin", "Expected keyword BEGIN");
        lexer.nextToken();

        while (!isTokenOfType(TokenType.KEYWORD) || !"end".equals(currentValue())) {
            node.addStatement(parseArchLine());
        }
        lexer.nextToken();

        checkType(TokenType.IDENT, "Expected identification");
        if (!table.getArchName().equals(currentValue())) {
            throw new ParserException("Architecture name does not match.");
        }
        lexer.nextToken();

        checkType(TokenType.SEMICOLON, "; expected");
        lexer.nextToken();

        return node;
    }

    private List<Declaration> parseInternalSignals() {
        List<String> declarations = new ArrayList<>();

        declarations.add(parseDeclaration());
        while (true) {
            if (!isTokenOfType(TokenType.COMMA)) {
                break;
            }
            lexer.nextToken();
            declarations.add(parseDeclaration());
        }

        checkType(TokenType.COLON, "Colon expected");
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "Keyword expected.");
        List<Declaration> signals;
        if (currentValue().equals("std_logic")) {
            signals = createStdSignals(declarations);
            lexer.nextToken();
        } else if (currentValue().equals("std_logic_vector")) {
            lexer.nextToken();
            signals = createVectorSignals(declarations);
        } else {
            throw new ParserException("std_logic or std_logic_vector expected.");
        }

        checkType(TokenType.SEMICOLON, "Semicolon expected.");
        lexer.nextToken();

        return signals;
    }

    private Statement parseArchLine() {
        Statement statement;
        Token token = lexer.getCurrentToken();

        String label = parseLabel();
        if (!table.addLabel(label)) {
            throw new ParserException("Label already declared.");
        }
        if (label == null) {
            lexer.seek(token);
        }

        if (isTokenOfType(TokenType.IDENT)) {
            DeclarationExpression indexer = parseSignal();

            checkType(TokenType.ASSIGN, "Assignment expected.");
            lexer.nextToken();

            ExpressionData expression = parseExpression(indexer.getDeclaration());
            statement =
                    new SetStatement(label, indexer, expression.expression, expression.sensitivity, expression.delay);
        } else if (isTokenOfType(TokenType.KEYWORD) && currentValue().equals("entity")) {
            lexer.nextToken();

            if (label == null) {
                throw new ParserException("Port map statement must contain label.");
            }

            statement = parseMapping(label);
        } else {
            throw new ParserException("Expected assignment or mapping");
        }

        return statement;
    }

    private Statement parseMapping(String label) {
        checkType(TokenType.KEYWORD, "work", "Expected keyword WORK.");
        lexer.nextToken();

        checkType(TokenType.DOT, "Dot expected");
        lexer.nextToken();

        checkType(TokenType.IDENT, "Entity identification expected.");
        String id = (String) currentValue();
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "port", "Keyword PORT expected.");
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "map", "Keyword MAP expected.");
        lexer.nextToken();

        checkType(TokenType.OPEN_PARENTHESES, "( expected");
        lexer.nextToken();

        Token token = lexer.getCurrentToken();
        testMappable();

        Statement map;
        if (isTokenOfType(TokenType.COMMA)) {
            lexer.seek(token);
            List<Mappable> signals = parsePositionalMapping();

            map = new Mapping.Positional(label, id, signals);
        } else if (isTokenOfType(TokenType.MAP)) {
            lexer.seek(token);
            Map<Declaration, Mappable> signals = parseAssociativeMapping();

            map = new Mapping.Associative(label, id, signals);
        } else {
            throw new ParserException("Invalid port map statement");
        }

        checkType(TokenType.SEMICOLON, "Semicolon expected");
        lexer.nextToken();

        return map;
    }

    private void testMappable() {
        String name = (String) currentValue();
        lexer.nextToken();

        if (!isTokenOfType(TokenType.OPEN_PARENTHESES)) {

            return;
        } else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
            lexer.nextToken();

            checkType(TokenType.NUMBER, "Expected number as index.");
            int position = (int) currentValue();
            lexer.nextToken();

            checkType(TokenType.CLOSED_PARENTHESES, "Expected )");
            lexer.nextToken();

            return;

        } else {
            throw new ParserException("Invalid signal type.");
        }
    }

    private Map<Declaration, Mappable> parseAssociativeMapping() {
        Map<Declaration, Mappable> signals = new HashMap<>();

        while (true) {
            Declaration signal1 = parseMappingSignal();

            checkType(TokenType.MAP, "Association operator expected");
            lexer.nextToken();

            if (isTokenOfType(TokenType.KEYWORD) && (currentValue()).equals("open")) {
                signals.put(signal1, null);
                lexer.nextToken();
            } else if (isTokenOfType(TokenType.CONSTANT) || isTokenOfType(TokenType.CONSTANT_VECTOR)) {
                signals.put(signal1, new Constant((LogicValue[]) currentValue(), lexer.getCurrentToken().getType()));
                lexer.nextToken();
            } else {
                signals.put(signal1, parseSignal());
            }

            if (isTokenOfType(TokenType.COMMA)) {
                lexer.nextToken();
                continue;
            } else if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
                lexer.nextToken();
                break;
            } else {
                throw new ParserException("Illegal sequence for port map statement");
            }
        }

        return signals;
    }

    private Declaration parseMappingSignal() {
        String name = (String) currentValue();
        lexer.nextToken();

        if (!isTokenOfType(TokenType.OPEN_PARENTHESES)) {

            return new Declaration(name);
        } else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
            lexer.nextToken();

            checkType(TokenType.NUMBER, "Expected number as index.");
            int position = (int) currentValue();
            lexer.nextToken();

            if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
                lexer.nextToken();

                return new Declaration(name, new VectorData(position, null, position));
            } else if (isTokenOfType(TokenType.KEYWORD)) {
                VectorOrder order;
                if (currentValue().equals("to")) {
                    order = VectorOrder.TO;
                } else if (currentValue().equals("downto")) {
                    order = VectorOrder.DOWNTO;
                } else {
                    throw new ParserException("TO or DOWNTO keyword expected.");
                }
                lexer.nextToken();

                checkType(TokenType.NUMBER, "Expected number");
                int second = (int) currentValue();
                lexer.nextToken();

                checkType(TokenType.CLOSED_PARENTHESES, "Expected closed parentheses");
                lexer.nextToken();

                return new Declaration(name, new VectorData(position, order, second));

            } else {
                throw new ParserException("Invalid signal type.");
            }
        } else {
            throw new ParserException("Invalid signal type.");
        }
    }

    private List<Mappable> parsePositionalMapping() {
        List<Mappable> signals = new ArrayList<>();

        while (true) {
            if (isTokenOfType(TokenType.KEYWORD) && (currentValue()).equals("open")) {
                signals.add(null);
                lexer.nextToken();
            } else if (isTokenOfType(TokenType.CONSTANT) || isTokenOfType(TokenType.CONSTANT_VECTOR)) {
                signals.add(new Constant((LogicValue[]) currentValue(), lexer.getCurrentToken().getType()));
                lexer.nextToken();
            } else {
                signals.add(parseSignal());
            }

            if (isTokenOfType(TokenType.COMMA)) {
                lexer.nextToken();
                continue;
            } else if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
                lexer.nextToken();
                break;
            } else {
                throw new ParserException("Illegal sequence for port map statement");
            }
        }

        return signals;
    }

    private String parseLabel() {
        if (!isTokenOfType(TokenType.IDENT)) {
            return null;
        }
        String label = (String) currentValue();
        lexer.nextToken();

        if (!isTokenOfType(TokenType.COLON)) {
            return null;
        }
        lexer.nextToken();

        return label;
    }

    private ExpressionData parseExpression(Declaration portDeclaration) {
        //Shunting-yard algorithm
        Stack<Expression> operands = new Stack<>();
        Stack<String> operators = new Stack<>();
        Set<DeclarationExpression> sensitivity = new HashSet<>();

        expression(operands, operators, sensitivity, portDeclaration);

        long delay = 0;
        if (isTokenOfType(TokenType.KEYWORD) && currentValue().equals("after")) {
            lexer.nextToken();
            delay = readDelay();
        }

        checkType(TokenType.SEMICOLON, "; expected");
        lexer.nextToken();

        if (operands.size() != 1) {
            throw new ParserException("Expression is not valid.");
        }

        return new ExpressionData(operands.pop(), sensitivity, delay);
    }

    private long readDelay() {
        checkType(TokenType.NUMBER, "Number expected");
        int number = (int) currentValue();
        lexer.nextToken();

        checkType(TokenType.IDENT, "Identifier for time unit expected.");
        String unit = (String) currentValue();

        long delay;
        if (unit.equals("ms")) {
            delay = number;
        } else if (unit.equals("s")) {
            delay = number * 1000;
        } else {
            throw new ParserException("Invalid time unit given.");
        }
        lexer.nextToken();

        return delay;
    }

    private void expression(
            Stack<Expression> operands, Stack<String> operators, Set<DeclarationExpression> sensitivity,
            Declaration portDeclaration) {
        term(operands, operators, sensitivity, portDeclaration);

        while (isTokenOfType(TokenType.OPERATORS) && !currentValue().equals("not")) {
            pushOperator((String) currentValue(), operators, operands);
            lexer.nextToken();
            term(operands, operators, sensitivity, portDeclaration);
        }
        while (!operators.empty() && !operators.peek().equals("(")) {
            popOperator(operands, operators);
        }
    }

    private void popOperator(Stack<Expression> operands, Stack<String> operators) {
        if (!operators.peek().equals("not")) {
            Expression first = operands.pop();
            Expression second = operands.pop();
            operands.push(OperatorFactory.getBinaryOperator(first, second, operators.pop()));
        } else {
            operands.push(OperatorFactory.getUnaryOperator(operands.pop(), operators.pop()));
        }
    }

    private void pushOperator(
            String operator, Stack<String> operators, Stack<Expression> operands) {
        while (!operators.empty() && operators.peek().equals("not")) {
            popOperator(operands, operators);
        }

        while (!operators.empty() && operators.peek().equals("&") &&
               !(operator.equals("(") || operator.equals("not"))) {
            popOperator(operands, operators);
        }

        if (!operators.empty()) {
            checkOperatorsOrder(operators.peek(), operator);
        }

        operators.push(operator);
    }

    private void term(
            Stack<Expression> operands, Stack<String> operators, Set<DeclarationExpression> sensitivity,
            Declaration portDeclaration) {
        if (isTokenOfType(TokenType.CONSTANT) || isTokenOfType(TokenType.CONSTANT_VECTOR)) {
            Constant constant = new Constant((LogicValue[]) currentValue(), lexer.getCurrentToken().getType());
            if (constant.getDeclaration().getType() != portDeclaration.getType()) {
                throw new ParserException("Invalid type for constant.");
            }
            if (constant.getDeclaration().getType() == Type.VECTOR_STD_LOGIC &&
                constant.getDeclaration().getVectorData().getSize() != portDeclaration.getVectorData().getSize()) {
                throw new ParserException("Constant vector is not valid size.");
            }

            operands.push(constant);
            lexer.nextToken();
        } else if (isTokenOfType(TokenType.IDENT)) {
            DeclarationExpression e = parseSignal();

            Declaration d = e.getDeclaration();
            if (d.getPortType() != null && d.getPortType() == PortType.OUT) {
                throw new ParserException("Only IN ports can exist on the right side of the " + "expression.");
            }

            sensitivity.add(e);
            operands.push(e);
        } else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
            lexer.nextToken();
            pushOperator("(", operators, operands);
            expression(operands, operators, sensitivity, portDeclaration);

            checkType(TokenType.CLOSED_PARENTHESES, "Expected )");
            operators.pop();
            lexer.nextToken();
        } else if (isTokenOfType(TokenType.OPERATORS) && currentValue().equals("not")) {
            pushOperator("not", operators, operands);
            lexer.nextToken();
            term(operands, operators, sensitivity, portDeclaration);
        } else {
            throw new ParserException("Invalid token.");
        }
    }

    private DeclarationExpression parseSignal() {
        String name = (String) currentValue();
        if (!table.isDeclared(name)) {
            throw new ParserException("Undeclared signal: " + name + ".");
        }

        Declaration declaration = table.getDeclaration(name);
        lexer.nextToken();

        if (declaration.getType() == Type.STD_LOGIC || !isTokenOfType(TokenType.OPEN_PARENTHESES)) {

            return new DeclarationExpression(declaration);
        } else if (declaration.getType() == Type.VECTOR_STD_LOGIC && isTokenOfType(TokenType.OPEN_PARENTHESES)) {
            lexer.nextToken();

            checkType(TokenType.NUMBER, "Expected number as index.");
            int position = (int) currentValue();
            lexer.nextToken();

            if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
                lexer.nextToken();

                return new IndexerOperator(table.getDeclaration(name), new VectorData(position, null, position));
            } else if (isTokenOfType(TokenType.KEYWORD)) {
                VectorOrder order;
                if (currentValue().equals("to")) {
                    order = VectorOrder.TO;
                } else if (currentValue().equals("downto")) {
                    order = VectorOrder.DOWNTO;
                } else {
                    throw new ParserException("TO or DOWNTO keyword epected.");
                }
                lexer.nextToken();

                checkType(TokenType.NUMBER, "Expected number");
                int second = (int) currentValue();
                lexer.nextToken();

                checkType(TokenType.CLOSED_PARENTHESES, "Expected closed parentheses");
                lexer.nextToken();

                return new IndexerOperator(table.getDeclaration(name), new VectorData(position, order, second));

            } else {
                throw new ParserException("Invalid signal type.");
            }
        } else {
            throw new ParserException("Invalid signal type.");
        }
    }

    private EntityNode parseEntity() {
        checkType(TokenType.IDENT, "Identifier expected.");
        EntityNode entity = new EntityNode((String) currentValue());
        table.setEntryName((String) currentValue());
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "is", "Expected keyword IS.");
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "port", "Expected keyword PORT");
        lexer.nextToken();

        checkType(TokenType.OPEN_PARENTHESES, "Expected (");
        lexer.nextToken();

        boolean last = false;
        while (!last) {
            while (isTokenOfType(TokenType.SEMICOLON)) {
                lexer.nextToken();
            }

            EntityLine line = parseEntityLine();
            entity.addSignals(line.signals);

            last = line.last;
        }

        checkType(TokenType.KEYWORD, "end", "Keyword END expected.");
        lexer.nextToken();

        checkType(TokenType.IDENT, entity.getName(), "Entity name expected.");
        if (!table.getEntryName().equals(currentValue())) {
            throw new ParserException("End entity name is different than given.");
        }
        lexer.nextToken();

        checkType(TokenType.SEMICOLON, "; expected");
        lexer.nextToken();

        return entity;
    }

    private EntityLine parseEntityLine() {
        List<String> declarations = new ArrayList<>();

        declarations.add(parseDeclaration());
        while (true) {
            if (!isTokenOfType(TokenType.COMMA)) {
                break;
            }
            lexer.nextToken();
            declarations.add(parseDeclaration());
        }

        checkType(TokenType.COLON, "Colon expected");
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "Keyword expected.");
        PortType type;
        if (currentValue().equals("in")) {
            type = PortType.IN;
        } else if (currentValue().equals("out")) {
            type = PortType.OUT;
        } else {
            throw new ParserException("IN or OUT keywords expected");
        }
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "Keyword expected.");
        List<Declaration> signals;
        if (currentValue().equals("std_logic")) {
            signals = createStdPorts(declarations, type);
            lexer.nextToken();
        } else if (currentValue().equals("std_logic_vector")) {
            lexer.nextToken();
            signals = createVectorPorts(declarations, type);
        } else {
            throw new ParserException("std_logic or std_logic_vector expected.");
        }

        if (isTokenOfType(TokenType.SEMICOLON)) {
            lexer.nextToken();
            return new EntityLine(signals, false);
        } else if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
            lexer.nextToken();
            if (isTokenOfType(TokenType.SEMICOLON)) {
                lexer.nextToken();
                return new EntityLine(signals, true);
            } else {
                throw new ParserException("Expected ;");
            }
        } else {
            throw new ParserException("Expected ; or );");
        }
    }

    private List<Declaration> createStdPorts(
            List<String> declarations, PortType type) {
        List<Declaration> ports = new ArrayList<>();
        declarations.forEach(s -> {
            Declaration port = new Declaration(s, type);
            ports.add(port);
            table.addDeclaration(port);
        });

        return ports;
    }

    private List<Declaration> createStdSignals(List<String> declarations) {
        List<Declaration> signals = new ArrayList<>();
        declarations.forEach(s -> {
            Declaration signal = new Declaration(s);
            signals.add(signal);
            table.addDeclaration(signal);
        });

        return signals;
    }

    private List<Declaration> createVectorSignals(
            List<String> declarations) {
        List<Declaration> signals = new ArrayList<>();
        VectorData data = readVectorData();

        for (String id : declarations) {
            Declaration signal = new Declaration(id, data);

            table.addDeclaration(signal);
            signals.add(signal);
        }

        return signals;
    }

    private List<Declaration> createVectorPorts(
            List<String> declarations, PortType type) {
        VectorData data = readVectorData();
        List<Declaration> ports = new ArrayList<>();

        declarations.forEach(s -> {
            Declaration port = new Declaration(s, type, data);

            table.addDeclaration(port);
            ports.add(port);
        });

        return ports;
    }

    private VectorData readVectorData() {
        checkType(TokenType.OPEN_PARENTHESES, "Open parentheses expected.");
        lexer.nextToken();

        checkType(TokenType.NUMBER, "Number expected.");
        int start = (int) currentValue();
        lexer.nextToken();

        checkType(TokenType.KEYWORD, "Keyword expected.");
        VectorOrder order;
        if (currentValue().equals("to")) {
            order = VectorOrder.TO;
        } else if (currentValue().equals("downto")) {
            order = VectorOrder.DOWNTO;
        } else {
            throw new ParserException("TO or DOWNTO keyword epected.");
        }
        lexer.nextToken();

        checkType(TokenType.NUMBER, "Number expected");
        int end = (int) currentValue();
        lexer.nextToken();

        checkType(TokenType.CLOSED_PARENTHESES, "Closed parentheses expected.");
        lexer.nextToken();

        return new VectorData(start, order, end);
    }

    private String parseDeclaration() {
        checkType(TokenType.IDENT, currentValue() + " is not valid identifier.");
        String value = (String) currentValue();
        lexer.nextToken();

        return value;
    }

    private void checkOperatorsOrder(String first, String second) {
        if (first.equals("(") || second.equals("(")) {
            return;
        }

        if (first.equals("&") || second.equals("&")) {
            return;
        }

        if (first.equals("not") && second.equals("not")) {
            throwOperatorsException(first, second);
        }

        if (first.equals("nand") && second.equals("nand")) {
            throwOperatorsException(first, second);
        }

        if (first.equals("nor") && second.equals("nor")) {
            throwOperatorsException(first, second);
        }

        if (!first.equals("not") && !second.equals("not") && !first.equals(second)) {
            throwOperatorsException(first, second);
        }
    }

    private void throwOperatorsException(String first, String second) {
        throw new ParserException("Illegal expression for operators: " + first + " " + "and " + second + ".");
    }

    private static class EntityLine {
        private List<Declaration> signals;
        private boolean last;

        public EntityLine(
                List<Declaration> signals, boolean last) {
            this.signals = signals;
            this.last = last;
        }
    }

    private static class ExpressionData {
        private Expression expression;
        private Set<DeclarationExpression> sensitivity;
        private long delay;

        public ExpressionData(
                Expression expression, Set<DeclarationExpression> sensitivity, long delay) {
            this.expression = expression;
            if (!expression.isValid()) {
                throw new ParserException("Expression is not valid.");
            }

            this.sensitivity = sensitivity;
            this.delay = delay;
        }
    }
}
