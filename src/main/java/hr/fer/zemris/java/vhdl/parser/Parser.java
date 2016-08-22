package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.Token;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Constant;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.OperatorFactory;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary.IndexerOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetElementStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		ProgramNode programNode = new ProgramNode();

		checkType(TokenType.KEYWORD, "entity", "Expected entity block.");
		lexer.nextToken();
		programNode.setEntity(parseEntity());

		checkType(TokenType.KEYWORD, "architecture", "Expected ARCHITECTURE block.");
		lexer.nextToken();
		programNode.setArchitecture(parseArchitecture());

		checkType(TokenType.EOF, "End of file expected.");

		return programNode;
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

			List<Signal> signals = parseInternalSignals();
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

	private List<Signal> parseInternalSignals() {
		List<String> declarations = new ArrayList<>();

		declarations.add(parseSignalDeclaration());
		while (true) {
			if (!isTokenOfType(TokenType.COMMA)) {
				break;
			}
			lexer.nextToken();
			declarations.add(parseSignalDeclaration());
		}

		checkType(TokenType.COLON, "Colon expected");
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		List<Signal> signals;
		if (currentValue().equals("std_logic")) {
			signals = createStdSignals(declarations, Signal.Type.INTERNAL);
			lexer.nextToken();
		} else if (currentValue().equals("std_logic_vector")) {
			lexer.nextToken();
			signals = createVectorSignals(declarations, Signal.Type.INTERNAL);
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
		if (label == null) {
			lexer.seek(token);
		}

		if (isTokenOfType(TokenType.IDENT)) {
			String id = (String) currentValue();
			lexer.nextToken();

			if (!table.containsSignal(id)) {
				throw new ParserException("Undefined signal: " + id + ". ");
			}
			Signal signal = table.getSignal(id);

			if (isTokenOfType(TokenType.ASSIGN)) {
				lexer.nextToken();

				ExpressionData expression = parseExpression(signal.getTypeOf());
				statement = new SetStatement(label, table.getSignal(id), expression.expression,
						expression.sensitivity);
			} else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
				IndexerOperator indexer = parseAccess(signal);

				checkType(TokenType.ASSIGN, "Assignment expected.");
				lexer.nextToken();

				ExpressionData expression = parseExpression(LogicValue.class);
				statement = new SetElementStatement(label, indexer, expression.expression,
						expression.sensitivity);
			} else {
				throw new ParserException("Expected signal or signal vector.");
			}
		} else {
			throw new ParserException("Expected assignment or mapping");
		}

		return statement;
	}

	private IndexerOperator parseAccess(Signal signal) {
		if (signal.getTypeOf() != Vector.class) {
			throw new ParserException("Cannot index non vector signal.");
		}

		checkType(TokenType.OPEN_PARENTHESES, "( expected");
		lexer.nextToken();

		checkType(TokenType.NUMBER, "Index as number expected.");
		int position = (int) currentValue();
		lexer.nextToken();

		checkType(TokenType.CLOSED_PARENTHESES, ") expected");
		lexer.nextToken();

		return new IndexerOperator(signal, position);
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

	private ExpressionData parseExpression(Class typeOf) {
		//Shunting-yard algorithm
		Stack<Expression> operands = new Stack<>();
		Stack<String> operators = new Stack<>();
		Set<Signal> sensitivity = new HashSet<>();

		expression(operands, operators, sensitivity, typeOf);

		checkType(TokenType.SEMICOLON, "; expected");
		lexer.nextToken();

		if (operands.size() != 1) {
			throw new ParserException("Expression is not valid.");
		}

		return new ExpressionData(operands.pop(), sensitivity);
	}

	private void expression(
			Stack<Expression> operands, Stack<String> operators, Set<Signal> sensitivity,
			Class typeOf) {
		term(operands, operators, sensitivity, typeOf);

		while (isTokenOfType(TokenType.OPERATORS) && !currentValue().equals("not")) {
			pushOperator((String) currentValue(), operators, operands);
			lexer.nextToken();
			term(operands, operators, sensitivity, typeOf);
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

		if(!operators.empty()) {
			checkOperatorsOrder(operators.peek(), operator);
		}

		operators.push(operator);
	}

	private void term(
			Stack<Expression> operands, Stack<String> operators, Set<Signal> sensitivity,
			Class typeOf) {
		if (isTokenOfType(TokenType.CONSTANT) || isTokenOfType(TokenType.CONSTANT_VECTOR)) {
			operands.push(new Constant((Value) currentValue()));
			lexer.nextToken();
		} else if (isTokenOfType(TokenType.IDENT)) {
			operands.push(parseSignal(sensitivity, typeOf));
		} else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
			lexer.nextToken();
			pushOperator("(", operators, operands);
			expression(operands, operators, sensitivity, typeOf);

			checkType(TokenType.CLOSED_PARENTHESES, "Expected )");
			operators.pop();
			lexer.nextToken();
		} else if (isTokenOfType(TokenType.OPERATORS) && currentValue().equals("not")) {
			pushOperator("not", operators, operands);
			lexer.nextToken();
			term(operands, operators, sensitivity, typeOf);
		} else {
			throw new ParserException("Invalid token.");
		}
	}

	private Expression parseSignal(Set<Signal> sensitivity, Class typeOf) {
		String name = (String) currentValue();
		if (!table.containsSignal(name)) {
			throw new ParserException("Undeclared signal: " + name + ".");
		}
		Signal signal = table.getSignal(name);
		sensitivity.add(signal);

		if (signal.getSignalType() == Signal.Type.OUT) {
			throw new ParserException(
					"Output signals cannot be written on the right side of" + " expression.");
		}

		lexer.nextToken();

		if (signal.getTypeOf() == LogicValue.class || !isTokenOfType(
				TokenType.OPEN_PARENTHESES)) {

			if (signal.getTypeOf() != typeOf) {
				throw new ParserException(
						"Signal " + signal.getId() + " is type of: " + signal.getTypeOf() + "."
						+ " Expected: " + typeOf);
			}

			return signal;
		} else if (signal.getTypeOf() == Vector.class && isTokenOfType(
				TokenType.OPEN_PARENTHESES)) {
			lexer.nextToken();

			checkType(TokenType.NUMBER, "Expected number as index.");
			int position = (int) currentValue();
			lexer.nextToken();

			checkType(TokenType.CLOSED_PARENTHESES, "Expected )");
			lexer.nextToken();

			if (typeOf != LogicValue.class) {
				throw new ParserException("Cannot assign logic value to vector.");
			}

			return new IndexerOperator(signal, position);

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
			entity.addSignals(line.signals, line.type);

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

		declarations.add(parseSignalDeclaration());
		while (true) {
			if (!isTokenOfType(TokenType.COMMA)) {
				break;
			}
			lexer.nextToken();
			declarations.add(parseSignalDeclaration());
		}

		checkType(TokenType.COLON, "Colon expected");
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		Signal.Type type;
		if (currentValue().equals("in")) {
			type = Signal.Type.IN;
		} else if (currentValue().equals("out")) {
			type = Signal.Type.OUT;
		} else {
			throw new ParserException("IN or OUT keywords expected");
		}
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		List<Signal> signals;
		if (currentValue().equals("std_logic")) {
			signals = createStdSignals(declarations, type);
			lexer.nextToken();
		} else if (currentValue().equals("std_logic_vector")) {
			lexer.nextToken();
			signals = createVectorSignals(declarations, type);
		} else {
			throw new ParserException("std_logic or std_logic_vector expected.");
		}

		if (isTokenOfType(TokenType.SEMICOLON)) {
			lexer.nextToken();
			return new EntityLine(signals, type, false);
		} else if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
			lexer.nextToken();
			if (isTokenOfType(TokenType.SEMICOLON)) {
				lexer.nextToken();
				return new EntityLine(signals, type, true);
			} else {
				throw new ParserException("Expected ;");
			}
		} else {
			throw new ParserException("Expected ; or );");
		}
	}

	private List<Signal> createVectorSignals(List<String> declarations, Signal.Type type) {
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

		List<Signal> signals = new ArrayList<>();
		for (String id : declarations) {
			Signal signal = new Signal(id, new Vector(start, order, end), type);
			table.addSignal(signal);
			signals.add(signal);
		}

		return signals;
	}

	private List<Signal> createStdSignals(List<String> declarations, Signal.Type type) {
		List<Signal> signals = new ArrayList<>();
		for (String id : declarations) {
			Signal stdLogic = new Signal(id, LogicValue.UNINITIALIZED, type);
			table.addSignal(stdLogic);
			signals.add(stdLogic);
		}

		return signals;
	}

	private String parseSignalDeclaration() {

		checkType(TokenType.IDENT, currentValue() + " is not valid identifier.");
		String value = (String) currentValue();
		lexer.nextToken();

		return value;
	}

	private void checkOperatorsOrder(String first, String second) {
		if (first.equals("(") || second.equals("(")) {
			return;
		}

		if (first.equals("not") && second.equals("not")) {
			throwOperatorsException(first, second);
		}

		if (first.equals("nand") && second.equals("nand")) {
			throwOperatorsException(first, second);

			if (first.equals("nor") && second.equals("nor")) {
				throwOperatorsException(first, second);
			}

			if (!first.equals("not") && !second.equals("not") && !first.equals(second)) {
				throwOperatorsException(first, second);
			}
		}
	}

	private void throwOperatorsException(String first, String second) {
		throw new ParserException(
				"Illegal expression for operators: " + first + " " + "and " + second + ".");
	}

	private static class EntityLine {
		private List<Signal> signals;
		private Signal.Type type;
		private boolean last;

		public EntityLine(List<Signal> signals, Signal.Type type, boolean last) {
			this.signals = signals;
			this.type = type;
			this.last = last;
		}
	}

	private static class ExpressionData {
		private Expression expression;
		private Set<Signal> sensitivity;

		public ExpressionData(
				Expression expression, Set<Signal> sensitivity) {
			this.expression = expression;
			this.sensitivity = sensitivity;
		}
	}
}
