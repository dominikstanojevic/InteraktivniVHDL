package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.DeclarationNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ExpressionNode;
import hr.fer.zemris.java.vhdl.parser.nodes.IExpressionElement;
import hr.fer.zemris.java.vhdl.parser.nodes.InputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.Operator;
import hr.fer.zemris.java.vhdl.parser.nodes.OutputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.Variable;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by Dominik on 25.7.2016..
 */
public class Parser {
	private Lexer lexer;
	private ProgramNode programNode;

	public Parser(Lexer lexer) {
		this.lexer = lexer;
		programNode = parse();
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
		programNode.addChild(parseEntity());

		while (!isTokenOfType(TokenType.EOF)) {
			checkType(TokenType.KEYWORD, "architecture", "Expected ARCHITECTURE block.");
			lexer.nextToken();
			programNode.addChild(parseArchitecture());
		}

		return programNode;
	}

	private ArchitectureNode parseArchitecture() {
		checkType(TokenType.IDENT, "Expected identification.");
		String name = (String) currentValue();
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "of", "Expected keyword OF");
		lexer.nextToken();

		checkType(TokenType.IDENT, "Expected identification.");
		String entity = (String) currentValue();
		ArchitectureNode node = new ArchitectureNode(name, entity);
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "is", "Expected keyword IS");
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "begin", "Expected keyword BEGIN");
		lexer.nextToken();

		while (!isTokenOfType(TokenType.KEYWORD) || !"end".equals(currentValue())) {
			node.addExpression(parseArchLine());
		}
		lexer.nextToken();

		checkType(TokenType.IDENT, "Expected identification");
		node.setEnd((String) currentValue());
		lexer.nextToken();

		checkType(TokenType.SEMICOLON, "; expected");
		lexer.nextToken();

		return node;
	}

	private ExpressionNode parseArchLine() {
		checkType(TokenType.IDENT, "Expected identification.");
		Variable variable = new Variable((String) currentValue());
		lexer.nextToken();

		checkType(TokenType.ASSIGN, "Expected <=");
		lexer.nextToken();

		return new ExpressionNode(variable, parseExpression());
	}

	private Queue<IExpressionElement> parseExpression() {
		//Shunting-yard algorithm
		Queue<IExpressionElement> output = new LinkedList<>();
		Stack<Operator> stack = new Stack<>();

		do {
			if (isTokenOfType(TokenType.IDENT)) {
				output.add(new Variable((String) currentValue()));
			} else if (isTokenOfType(TokenType.OPERATORS)) {
				while (!stack.empty() && stack.peek().getName().equals("not")) {
					output.add(stack.pop());
				}

				stack.add(new Operator((String) currentValue()));
			} else if (isTokenOfType(TokenType.OPEN_PARENTHESES)) {
				stack.add(new Operator("("));
			} else if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
				try {
					while (!stack.peek().getName().equals("(")) {
						output.add(stack.pop());
					}

					stack.pop();
				} catch (EmptyStackException e) {
					throw new ParserException("Invalid expression. ( missing.");
				}
			} else if (isTokenOfType(TokenType.SEMICOLON)) {
				lexer.nextToken();
				break;
			} else {
				throw new ParserException("Invalid expression");
			}

			lexer.nextToken();
		} while (true);

		while (!stack.empty()) {
			output.add(stack.pop());
		}

		return output;
	}

	private EntityNode parseEntity() {
		checkType(TokenType.IDENT, "Identifier expected.");
		EntityNode entity = new EntityNode((String) currentValue());
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
			entity.addDeclarationNode(line.definition);

			last = line.last;
		}

		checkType(TokenType.KEYWORD, "end", "Keyword END expected.");
		lexer.nextToken();

		checkType(TokenType.IDENT, entity.getName(), "Entity name expected.");
		entity.setEnd((String) currentValue());
		lexer.nextToken();

		checkType(TokenType.SEMICOLON, "; expected");
		lexer.nextToken();

		return entity;
	}

	private EntityLine parseEntityLine() {
		List<Variable> variables = new ArrayList<>();

		variables.add(parseVariable());
		while (true) {
			if (!isTokenOfType(TokenType.COMMA)) {
				break;
			}
			lexer.nextToken();
			variables.add(parseVariable());
		}

		checkType(TokenType.COLON, "Colon expected");
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		DeclarationNode line;
		if (currentValue().equals("in")) {
			line = new InputNode(variables);
		} else if (currentValue().equals("out")) {
			line = new OutputNode(variables);
		} else {
			throw new ParserException("IN or OUT keywords expected");
		}
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "std_logic", "std_logic keyword expected");
		lexer.nextToken();

		if (isTokenOfType(TokenType.SEMICOLON)) {
			lexer.nextToken();
			return new EntityLine(line, false);
		} else if (isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
			lexer.nextToken();
			if (isTokenOfType(TokenType.SEMICOLON)) {
				lexer.nextToken();
				return new EntityLine(line, true);
			} else {
				throw new ParserException("Expected ;");
			}
		} else {
			throw new ParserException("Expected ; or );");
		}
	}

	private Variable parseVariable() {

		checkType(TokenType.IDENT, currentValue() + " is not valid identifier.");
		Variable variable = new Variable((String) currentValue());
		lexer.nextToken();

		return variable;
	}

	private static class EntityLine {
		DeclarationNode definition;
		boolean last;

		public EntityLine(DeclarationNode definition, boolean last) {
			this.definition = definition;
			this.last = last;
		}
	}
}
