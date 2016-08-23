package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.Token;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.models.mappers.AssociativeMap;
import hr.fer.zemris.java.vhdl.models.mappers.EntityMap;
import hr.fer.zemris.java.vhdl.models.mappers.PositionalMap;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Constant;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.OperatorFactory;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalDeclaration;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalExpression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary.IndexerOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetElementStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

			Map<String, SignalDeclaration> signals = parseInternalSignals();
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

	private Map<String, SignalDeclaration> parseInternalSignals() {
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
		Map<String, SignalDeclaration> signals;
		if (currentValue().equals("std_logic")) {
			signals = createStdSignals(declarations, SignalDeclaration.Type.INTERNAL);
			lexer.nextToken();
		} else if (currentValue().equals("std_logic_vector")) {
			lexer.nextToken();
			signals = createVectorSignals(declarations, SignalDeclaration.Type.INTERNAL);
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
		if(!table.addLabel(label)) {
			throw new ParserException("Label already declared.");
		}
		if (label == null) {
			lexer.seek(token);
		}

		if (isTokenOfType(TokenType.IDENT)) {
			String id = (String) currentValue();
			lexer.nextToken();

			if (!table.containsSignal(id)) {
				throw new ParserException("Undefined signal: " + id + ". ");
			}
			SignalDeclaration signalDeclaration = table.getSignal(id);

			if (isTokenOfType(TokenType.ASSIGN)) {
				lexer.nextToken();

				ExpressionData expression = parseExpression(signalDeclaration);
				statement = new SetStatement(label, id, expression.expression,
						expression.sensitivity);
			} else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
				IndexerOperator indexer = parseAccess(id);

				checkType(TokenType.ASSIGN, "Assignment expected.");
				lexer.nextToken();

				ExpressionData expression = parseExpression(new SignalDeclaration(
						table.getDeclaration(indexer.getSignal()).getSignalType()));
				statement = new SetElementStatement(label, indexer.getSignal(),
						expression.expression, expression.sensitivity, indexer.getPosition());
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
		lexer.nextToken();

		EntityMap map;
		if (isTokenOfType(TokenType.COMMA)) {
			lexer.seek(token);
			List<String> signals = parsePositionalMapping();

			map = new PositionalMap(label, id, signals);
		} else if (isTokenOfType(TokenType.MAP)) {
			lexer.seek(token);
			Map<String, String> signals = parseAssociativeMapping();

			map = new AssociativeMap(label, id, signals);
		} else {
			throw new ParserException("Invalid port map statement");
		}

		checkType(TokenType.SEMICOLON, "Semicolon expected");
		lexer.nextToken();

		return map;
	}

	private Map<String, String> parseAssociativeMapping() {
		Map<String, String> signals = new HashMap<>();

		while (true) {
			String signal1 = (String) currentValue();
			lexer.nextToken();

			checkType(TokenType.MAP, "Association operator expected");
			lexer.nextToken();

			String signal2 = (String) currentValue();
			if (signal2.equals("open")) {
				signals.put(signal1, null);
			} else if (table.containsSignal(signal2)) {
				signals.put(signal1, signal2);
			} else {
				throw new ParserException("Signal " + signal2 + " is not declared.");
			}
			lexer.nextToken();

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

	private List<String> parsePositionalMapping() {
		List<String> signals = new ArrayList<>();

		while (true) {
			String signal = (String) currentValue();
			if (signal.equals("open")) {
				signals.add(null);
			} else if (table.containsSignal(signal)) {
				signals.add(signal);
			} else {
				throw new ParserException("Signal " + signal + " is not declared.");
			}
			lexer.nextToken();

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
		if (table.containsSignal(signal)
			&& table.getSignal(signal).getTypeOf() != Vector.class) {
			throw new ParserException("Cannot index non vector signal.");
		}

		checkType(TokenType.OPEN_PARENTHESES, "( expected");
		lexer.nextToken();

		checkType(TokenType.NUMBER, "Index as number expected.");
		int position = (int) currentValue();
		lexer.nextToken();

		checkType(TokenType.CLOSED_PARENTHESES, ") expected");
		lexer.nextToken();

		return new IndexerOperator(new SignalExpression(signal), position);
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

	private ExpressionData parseExpression(SignalDeclaration signalDeclaration) {
		//Shunting-yard algorithm
		Stack<Expression> operands = new Stack<>();
		Stack<String> operators = new Stack<>();
		Set<String> sensitivity = new HashSet<>();

		expression(operands, operators, sensitivity, signalDeclaration);

		checkType(TokenType.SEMICOLON, "; expected");
		lexer.nextToken();

		if (operands.size() != 1) {
			throw new ParserException("Expression is not valid.");
		}

		return new ExpressionData(operands.pop(), sensitivity);
	}

	private void expression(
			Stack<Expression> operands, Stack<String> operators, Set<String> sensitivity,
			SignalDeclaration signalDeclaration) {
		term(operands, operators, sensitivity, signalDeclaration);

		while (isTokenOfType(TokenType.OPERATORS) && !currentValue().equals("not")) {
			pushOperator((String) currentValue(), operators, operands);
			lexer.nextToken();
			term(operands, operators, sensitivity, signalDeclaration);
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

		if (!operators.empty()) {
			checkOperatorsOrder(operators.peek(), operator);
		}

		operators.push(operator);
	}

	private void term(
			Stack<Expression> operands, Stack<String> operators, Set<String> sensitivity,
			SignalDeclaration signalDeclaration) {
		if (isTokenOfType(TokenType.CONSTANT) || isTokenOfType(TokenType.CONSTANT_VECTOR)) {
			operands.push(new Constant((Value) currentValue()));
			lexer.nextToken();
		} else if (isTokenOfType(TokenType.IDENT)) {
			operands.push(parseSignal(sensitivity, signalDeclaration));
		} else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
			lexer.nextToken();
			pushOperator("(", operators, operands);
			expression(operands, operators, sensitivity, signalDeclaration);

			checkType(TokenType.CLOSED_PARENTHESES, "Expected )");
			operators.pop();
			lexer.nextToken();
		} else if (isTokenOfType(TokenType.OPERATORS) && currentValue().equals("not")) {
			pushOperator("not", operators, operands);
			lexer.nextToken();
			term(operands, operators, sensitivity, signalDeclaration);
		} else {
			throw new ParserException("Invalid token.");
		}
	}

	private Expression parseSignal(
			Set<String> sensitivity, SignalDeclaration signalDeclaration) {
		String name = (String) currentValue();
		if (!table.containsSignal(name)) {
			throw new ParserException("Undeclared signal: " + name + ".");
		}
		SignalDeclaration declaration = table.getDeclaration(name);
		sensitivity.add(name);

		if (declaration.getSignalType() == SignalDeclaration.Type.OUT) {
			throw new ParserException(
					"Output signals cannot be written on the right side of" + " expression.");
		}

		lexer.nextToken();

		if (declaration.getTypeOf() == LogicValue.class || !isTokenOfType(
				TokenType.OPEN_PARENTHESES)) {

			if (declaration.getTypeOf() != signalDeclaration.getTypeOf()) {
				throw new ParserException(
						"Signal " + name + " is type of: " + declaration.getTypeOf()
						+ ", expected " + signalDeclaration.getTypeOf() + ".");
			}

			return new SignalExpression(name);
		} else if (declaration.getTypeOf() == Vector.class && isTokenOfType(
				TokenType.OPEN_PARENTHESES)) {
			lexer.nextToken();

			checkType(TokenType.NUMBER, "Expected number as index.");
			int position = (int) currentValue();
			lexer.nextToken();

			checkType(TokenType.CLOSED_PARENTHESES, "Expected )");
			lexer.nextToken();

			if (signalDeclaration.getTypeOf() != LogicValue.class) {
				throw new ParserException("Cannot assign logic value to vector.");
			}

			return new IndexerOperator(new SignalExpression(name), position);

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
		SignalDeclaration.Type type;
		if (currentValue().equals("in")) {
			type = SignalDeclaration.Type.IN;
		} else if (currentValue().equals("out")) {
			type = SignalDeclaration.Type.OUT;
		} else {
			throw new ParserException("IN or OUT keywords expected");
		}
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		Map<String, SignalDeclaration> signals;
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

	private Map<String, SignalDeclaration> createVectorSignals(
			List<String> declarations, SignalDeclaration.Type type) {
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

		Map<String, SignalDeclaration> signals = new LinkedHashMap<>();
		SignalDeclaration signal = new SignalDeclaration(type, start, order, end);
		for (String id : declarations) {
			table.addSignal(id, signal);
			signals.put(id, signal);
		}

		return signals;
	}

	private Map<String, SignalDeclaration> createStdSignals(
			List<String> declarations, SignalDeclaration.Type type) {
		Map<String, SignalDeclaration> signals = new LinkedHashMap<>();
		SignalDeclaration stdLogic = new SignalDeclaration(type);
		for (String id : declarations) {
			table.addSignal(id, stdLogic);
			signals.put(id, stdLogic);
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
		private Map<String, SignalDeclaration> signals;
		private boolean last;

		public EntityLine(
				Map<String, SignalDeclaration> signals, SignalDeclaration.Type type, boolean last) {
			this.signals = signals;
			this.last = last;
		}
	}

	private static class ExpressionData {
		private Expression expression;
		private Set<String> sensitivity;

		public ExpressionData(
				Expression expression, Set<String> sensitivity) {
			this.expression = expression;
			this.sensitivity = sensitivity;
		}
	}
}
