package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.INode;
import hr.fer.zemris.java.vhdl.parser.nodes.InputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.OutputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.VariableNode;

import java.util.ArrayList;
import java.util.List;

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

		//checkType(TokenType.KEYWORD, "architecture", "Expected ARCHITECTURE block.");
		//lexer.nextToken();
		//programNode.addChild(parseArchitecture());

		return programNode;
	}

	private ArchitectureNode parseArchitecture() {
		return null;
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
		while(!last) {
			while(isTokenOfType(TokenType.SEMICOLON)) {
				lexer.nextToken();
			}

			EntityLine line = parseEntityLine();
			if(line.definition instanceof InputNode) {
				entity.addInput((InputNode) line.definition);
			} else if (line.definition instanceof OutputNode) {
				entity.addOutput((OutputNode) line.definition);
			} else {
				throw new ParserException("Illegal input/output definition");
			}

			last = line.last;
		}

		return null;
	}

	private EntityLine parseEntityLine() {
		List<VariableNode> variables = new ArrayList<>();

		variables.add(parseVariable());
		while(true) {
			if(!isTokenOfType(TokenType.COMMA)) {
				break;
			}
			lexer.nextToken();
			variables.add(parseVariable());
		}

		checkType(TokenType.COLON, "Colon expected");
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "Keyword expected.");
		INode line;
		if(currentValue().equals("in")) {
			line = new InputNode(variables);
		} else if (currentValue().equals("out")) {
			line = new OutputNode(variables);
		} else {
			throw new ParserException("IN or OUT keywords expected");
		}
		lexer.nextToken();

		checkType(TokenType.KEYWORD, "std_logic", "std_logic keyword expected");
		lexer.nextToken();

		if(isTokenOfType(TokenType.SEMICOLON)) {
			lexer.nextToken();
			return new EntityLine(line, false);
		} else if(isTokenOfType(TokenType.CLOSED_PARENTHESES)) {
			lexer.nextToken();
			if(isTokenOfType(TokenType.SEMICOLON)) {
				lexer.nextToken();
				return new EntityLine(line, true);
			} else {
				throw new ParserException("Expected ;");
			}
		} else {
			throw new ParserException("Expected ; or );");
		}
	}

	private VariableNode parseVariable() {

		checkType(TokenType.IDENT, currentValue() + " is not valid identifier.");
		VariableNode variable = new VariableNode((String) currentValue());
		lexer.nextToken();

		return variable;
	}

	private static class EntityLine {
		INode definition;
		boolean last;

		public EntityLine(INode definition, boolean last) {
			this.definition = definition;
			this.last = last;
		}
	}
}
