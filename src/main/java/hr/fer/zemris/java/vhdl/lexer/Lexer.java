package hr.fer.zemris.java.vhdl.lexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dominik on 22.7.2016..
 */
public class Lexer {
	private char[] data;
	private int currPos;
	private Token currentToken;

	private static final Map<String, TokenType> mapper = new HashMap<>();

	static {
		mapper.put(":", TokenType.COLON);
		mapper.put(";", TokenType.SEMICOLON);
		mapper.put(",", TokenType.COMMA);
		mapper.put("(", TokenType.OPEN_PARENTHESES);
		mapper.put(")", TokenType.CLOSED_PARENTHESES);
		mapper.put("<=", TokenType.ASSIGN);
	}

	private static final Set<String> operators = new HashSet<>();

	static {
		operators.add("and");
		operators.add("or");
		operators.add("xor");
		operators.add("nand");
		operators.add("nor");
		operators.add("not");
	}

	private static final Set<String> keywords = new HashSet<>();

	static {
		keywords.add("entity");
		keywords.add("is");
		keywords.add("port");
		keywords.add("in");
		keywords.add("out");
		keywords.add("std_logic");
		keywords.add("end");
		keywords.add("architecture");
		keywords.add("of");
		keywords.add("begin");
	}

	public Lexer(String program) {
		data = program.toCharArray();
		extractNextToken();
	}

	public Token getCurrentToken() {
		return currentToken;
	}

	public Token nextToken() {
		extractNextToken();
		return getCurrentToken();
	}

	private void extractNextToken() {
		if (currentToken != null && currentToken.getType() == TokenType.EOF) {
			throw new LexerException("No tokens available.");
		}

		skipBlanks();

		if (currPos >= data.length) {
			currentToken = new Token(TokenType.EOF, null);
			return;
		}

		if (Character.isLetter(data[currPos])) {
			scanIdent();
			return;
		}

		if (isMapped(data[currPos])) {
			scanMapped();
			return;
		}

		if (isConstant(data[currPos])) {
			scanConstant();
			return;
		}

		throw new LexerException(String.format("Invalid character found: %c.", data[currPos]));
	}

	private void scanConstant() {
		//skipping '
		currPos++;
		int startIndex = currPos;

		while (currPos < data.length && data[currPos] != '\'') {
			currPos++;
		}

		int endIndex = currPos;
		//again skipping '
		currPos++;

		//it's a signal
		if (endIndex - startIndex == 1) {
			currentToken =
					new Token(TokenType.CONSTANT, Character.toLowerCase(data[startIndex]));
			return;
		}

		//it's a vector
		String value = new String(data, startIndex, endIndex - startIndex);
		currentToken = new Token(TokenType.CONSTANT_VECTOR, value);
	}

	private boolean isConstant(char c) {
		return c == '\'';

	}

	private void scanMapped() {
		String s = Character.toString(data[currPos++]);

		if (mapper.containsKey(s)) {
			currentToken = new Token(mapper.get(s), null);
			return;
		}

		//then it's <=
		currentToken = new Token(mapper.get(s + data[currPos++]), null);
	}

	private boolean isMapped(char c) {
		String s = Character.toString(c);

		if (mapper.containsKey(s)) {
			return true;
		}

		//check for <=
		int tempPos = currPos + 1;
		if (tempPos < data.length && c == '<' && data[tempPos] == '=') {
			return true;
		}

		return false;
	}

	private void scanIdent() {
		int startIndex = currPos;

		while (currPos < data.length && isWordCharacter(data[currPos])) {
			currPos++;
		}

		int endIndex = currPos;
		String value = new String(data, startIndex, endIndex - startIndex);

		if (keywords.contains(value.toLowerCase())) {
			currentToken = new Token(TokenType.KEYWORD, value.toLowerCase());
			return;
		}

		if (operators.contains(value.toLowerCase())) {
			currentToken = new Token(TokenType.OPERATORS, value.toLowerCase());
			return;
		}

		currentToken = new Token(TokenType.IDENT, value);
	}

	private void skipBlanks() {
		while (currPos < data.length) {
			char c = data[currPos];
			if (Character.isWhitespace(c)) {
				currPos++;
				continue;
			}

			break;
		}
	}

	private boolean isWordCharacter(char c) {
		if (Character.isLetter(c))
			return true;
		if (Character.isDigit(c))
			return true;
		if (c == '_')
			return true;

		return false;
	}
}
