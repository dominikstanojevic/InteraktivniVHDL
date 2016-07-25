package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;

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

	private ProgramNode parse() {
		ProgramNode programNode = new ProgramNode();

		if(!isTokenOfType(TokenType.KEYWORD) && lexer.getCurrentToken().getValue().equals
				("entity")) {
			throw new ParserException("Expected entity block");
		}
		lexer.nextToken();
		programNode.addChild(parseEntity());
		
		if(!isTokenOfType(TokenType.KEYWORD) && lexer.getCurrentToken().getValue().equals
				("architecture")) {
			throw new ParserException("Expected ARCHITECTURE block");
		}
		lexer.nextToken();
		programNode.addChild(parseArchitecture());

		return programNode;
	}

	private ArchitectureNode parseArchitecture() {
		return null;
	}

	private EntityNode parseEntity() {
		return null;
	}
}
