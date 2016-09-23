package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.Token;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.models.declarable.Declarable;
import hr.fer.zemris.java.vhdl.models.declarable.Port;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortDeclaration;
import hr.fer.zemris.java.vhdl.models.declarations.SignalDeclaration;
import hr.fer.zemris.java.vhdl.models.mappers.AssociativeMap;
import hr.fer.zemris.java.vhdl.models.mappers.EntityMap;
import hr.fer.zemris.java.vhdl.models.mappers.Mappable;
import hr.fer.zemris.java.vhdl.models.mappers.PositionalMap;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Constant;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.OperatorFactory;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalExpression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary.IndexerOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetElementStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
			throw new ParserException(
					"Expected: " + value + ". Got: " + lexer.getCurrentToken().getValue());
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
			throw new ParserException(
					"Model name in the architecture does not match entity " + "name.");
		}
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "is", "Expected keyword IS");
		lexer.nextToken();

		ArchitectureNode node = new ArchitectureNode(name);
		table.setArchName(name);

		while (isTokenOfType(TokenType.KEYWORD) && "signal".equals(currentValue())) {
			lexer.nextToken();

			Set<Signal> signals = parseInternalSignals();
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

	private Set<Signal> parseInternalSignals() {
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
		Set<Signal> signals;
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
			String id = (String) currentValue();
			lexer.nextToken();

			if (!table.isDeclared(id)) {
				throw new ParserException("Undefined signal: " + id + ". ");
			}
			Declaration portDeclaration = table.getDeclaration(id);

			if (isTokenOfType(TokenType.ASSIGN)) {
				lexer.nextToken();

				ExpressionData expression = parseExpression(portDeclaration);
				statement =
						new SetStatement(label, table.getDeclarable(id), expression.expression,
								expression.sensitivity, expression.delay);
			} else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
				IndexerOperator indexer = parseAccess(id);

				checkType(TokenType.ASSIGN, "Assignment expected.");
				lexer.nextToken();

				ExpressionData expression =
						parseExpression(SignalDeclaration.getLogicDeclaration());
				statement =
						new SetElementStatement(label, indexer.getId(), expression.expression,
								expression.sensitivity, indexer.getPosition(), expression.delay);
			} else {
				throw new ParserException("Expected signal or signal vector.");
			}
		} else if (isTokenOfType(TokenType.KEYWORD) && "entity".equals(currentValue())) {
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

	private EntityMap parseMapping(String label) {
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
		if (!(isTokenOfType(TokenType.CONSTANT) || isTokenOfType(TokenType.CONSTANT_VECTOR))) {
			Mappable test = parseSignal();
		} else {
			lexer.nextToken();
		}

		EntityMap map;
		if (isTokenOfType(TokenType.COMMA)) {
			lexer.seek(token);
			List<Mappable> signals = parsePositionalMapping();

			map = new PositionalMap(label, id, signals);
		} else if (isTokenOfType(TokenType.MAP)) {
			lexer.seek(token);
			Map<String, Mappable> signals = parseAssociativeMapping();

			map = new AssociativeMap(label, id, signals);
		} else {
			throw new ParserException("Invalid port map statement");
		}

		checkType(TokenType.SEMICOLON, "Semicolon expected");
		lexer.nextToken();

		return map;
	}

	private Map<String, Mappable> parseAssociativeMapping() {
		Map<String, Mappable> signals = new HashMap<>();

		while (true) {
			String signal1 = (String) currentValue();
			lexer.nextToken();

			checkType(TokenType.MAP, "Association operator expected");
			lexer.nextToken();

			if (isTokenOfType(TokenType.KEYWORD) && (currentValue()).equals("open")) {
				signals.put(signal1, null);
				lexer.nextToken();
			} else if (isTokenOfType(TokenType.CONSTANT) ||
					   isTokenOfType(TokenType.CONSTANT_VECTOR)) {
				signals.put(signal1, new Constant((Value) currentValue()));
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

	private List<Mappable> parsePositionalMapping() {
		List<Mappable> signals = new ArrayList<>();

		while (true) {
			if (isTokenOfType(TokenType.KEYWORD) && (currentValue()).equals("open")) {
				signals.add(null);
				lexer.nextToken();
			} else if (isTokenOfType(TokenType.CONSTANT) ||
					   isTokenOfType(TokenType.CONSTANT_VECTOR)) {
				signals.add(new Constant((Value) currentValue()));
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

	private IndexerOperator parseAccess(String signal) {
		if (table.isDeclared(signal) &&
			table.getDeclaration(signal).getTypeOf() != Value.TypeOf.STD_LOGIC_VECTOR) {
			throw new ParserException("Cannot index non vector signal.");
		}

		checkType(TokenType.OPEN_PARENTHESES, "( expected");
		lexer.nextToken();

		checkType(TokenType.NUMBER, "Index as number expected.");
		int position = (int) currentValue();
		lexer.nextToken();

		checkType(TokenType.CLOSED_PARENTHESES, ") expected");
		lexer.nextToken();

		return new IndexerOperator(table.getDeclarable(signal), position);
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

	private int parsePosition() {
		checkType(TokenType.OPEN_PARENTHESES, "( expected");
		lexer.nextToken();

		checkType(TokenType.NUMBER, "Expected element index as number.");
		int position = (int) currentValue();
		lexer.nextToken();

		checkType(TokenType.CLOSED_PARENTHESES, ") expected");
		lexer.nextToken();

		return position;
	}

	private ExpressionData parseExpression(Declaration portDeclaration) {
		//Shunting-yard algorithm
		Stack<Expression> operands = new Stack<>();
		Stack<String> operators = new Stack<>();
		Set<Declarable> sensitivity = new HashSet<>();

		expression(operands, operators, sensitivity, portDeclaration);

		long delay = 0;
		if(isTokenOfType(TokenType.KEYWORD) && currentValue().equals("after")) {
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
		if(unit.equals("ms")) {
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
			Stack<Expression> operands, Stack<String> operators, Set<Declarable> sensitivity,
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
			Stack<Expression> operands, Stack<String> operators, Set<Declarable> sensitivity,
			Declaration portDeclaration) {
		if (isTokenOfType(TokenType.CONSTANT) || isTokenOfType(TokenType.CONSTANT_VECTOR)) {
			Constant constant = new Constant((Value) currentValue());
			if (constant.getDeclaration().getTypeOf() != portDeclaration.getTypeOf()) {
				throw new ParserException("Invalid type for constant.");
			}
			if (constant.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR &&
				constant.getDeclaration().size() != portDeclaration.size()) {
				throw new ParserException("Constant vector is not valid size.");
			}

			operands.push(constant);
			lexer.nextToken();
		} else if (isTokenOfType(TokenType.IDENT)) {
			SignalExpression e = parseSignal();

			Declaration d = e.getDeclaration();
			if (d instanceof PortDeclaration &&
				((PortDeclaration) d).getPortType() == PortDeclaration.Type.OUT) {
				throw new ParserException(
						"Only IN ports can exist on the right side of the " + "expression.");
			}

			sensitivity.add(e.getId());
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

	private void checkIfValid(SignalExpression expression, Declaration portDeclaration) {
		Declaration declaration = expression.getDeclaration();

		if (declaration instanceof IndexerOperator &&
			portDeclaration.getTypeOf() != Value.TypeOf.STD_LOGIC) {
			throw new ParserException("Cannot assign logic value to vector.");
		}

		if (declaration.getTypeOf() != portDeclaration.getTypeOf()) {
			throw new ParserException("Signal " + declaration.toString() + " is type of: " +
									  declaration.getTypeOf() + ", expected " +
									  portDeclaration.getTypeOf() + ".");
		}
	}

	private SignalExpression parseSignal() {
		String name = (String) currentValue();
		if (!table.isDeclared(name)) {
			throw new ParserException("Undeclared signal: " + name + ".");
		}

		Declaration declaration = table.getDeclaration(name);
		lexer.nextToken();

		if (declaration.getTypeOf() == Value.TypeOf.STD_LOGIC ||
			!isTokenOfType(TokenType.OPEN_PARENTHESES)) {

			return new SignalExpression(table.getDeclarable(name));
		} else if (declaration.getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR &&
				   isTokenOfType(TokenType.OPEN_PARENTHESES)) {
			lexer.nextToken();

			checkType(TokenType.NUMBER, "Expected number as index.");
			int position = (int) currentValue();
			lexer.nextToken();

			checkType(TokenType.CLOSED_PARENTHESES, "Expected )");
			lexer.nextToken();

			return new IndexerOperator(table.getDeclarable(name), position);

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
		PortDeclaration.Type type;
		if (currentValue().equals("in")) {
			type = PortDeclaration.Type.IN;
		} else if (currentValue().equals("out")) {
			type = PortDeclaration.Type.OUT;
		} else {
			throw new ParserException("IN or OUT keywords expected");
		}
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		Set<Port> signals;
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

	private Set<Port> createStdPorts(
			List<String> declarations, PortDeclaration.Type type) {
		Set<Port> ports = new LinkedHashSet<>();
		PortDeclaration declaration = PortDeclaration.getLogicDeclaration(type);
		declarations.forEach(s -> {
			Port port = new Port(s, declaration);
			ports.add(port);
			table.addDeclaration(port);
		});

		return ports;
	}

	private Set<Signal> createStdSignals(List<String> declarations) {
		Set<Signal> signals = new LinkedHashSet<>();
		SignalDeclaration declaration = SignalDeclaration.getLogicDeclaration();
		declarations.forEach(s -> {
			Signal signal = new Signal(s, declaration);
			signals.add(signal);
			table.addDeclaration(signal);
		});

		return signals;
	}

	private Set<Signal> createVectorSignals(
			List<String> declarations) {

		Vector.VectorData data = readVectorData();
		Set<Signal> signals = new LinkedHashSet<>();

		SignalDeclaration declaration =
				new SignalDeclaration(data.getStart(), data.getOrder(), data.getEnd());

		for (String id : declarations) {
			Signal signal = new Signal(id, declaration);

			table.addDeclaration(signal);
			signals.add(signal);
		}

		return signals;
	}

	private Set<Port> createVectorPorts(
			List<String> declrations, PortDeclaration.Type type) {
		Vector.VectorData data = readVectorData();
		Set<Port> ports = new LinkedHashSet<>();

		PortDeclaration declaration =
				new PortDeclaration(type, data.getStart(), data.getOrder(), data.getEnd());

		declrations.forEach(s -> {
			Port port = new Port(s, declaration);

			table.addDeclaration(port);
			ports.add(port);
		});

		return ports;
	}

	private Vector.VectorData readVectorData() {
		checkType(TokenType.OPEN_PARENTHESES, "Open parentheses expected.");
		lexer.nextToken();

		checkType(TokenType.NUMBER, "Number expected.");
		int start = (int) currentValue();
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		Vector.Order order;
		if (currentValue().equals("to")) {
			order = Vector.Order.TO;
		} else if (currentValue().equals("downto")) {
			order = Vector.Order.DOWNTO;
		} else {
			throw new ParserException("TO or DOWNTO keyword epected.");
		}
		lexer.nextToken();

		checkType(TokenType.NUMBER, "Number expected");
		int end = (int) currentValue();
		lexer.nextToken();

		checkType(TokenType.CLOSED_PARENTHESES, "Closed parentheses expected.");
		lexer.nextToken();

		return new Vector.VectorData(start, order, end);
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
		throw new ParserException(
				"Illegal expression for operators: " + first + " " + "and " + second + ".");
	}

	private static class EntityLine {
		private Set<Port> signals;
		private boolean last;

		public EntityLine(
				Set<Port> signals, boolean last) {
			this.signals = signals;
			this.last = last;
		}
	}

	private static class ExpressionData {
		private Expression expression;
		private Set<Declarable> sensitivity;
		private long delay;

		public ExpressionData(
				Expression expression, Set<Declarable> sensitivity, long delay) {
			this.expression = expression;
			this.sensitivity = sensitivity;
			this.delay = delay;
		}
	}
}
