package hr.fer.zemris.java.vhdl.lexer;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dominik on 22.7.2016..
 */
public class Lexer {
	private char[] data;
	private int currPos;

	private List<Token> tokens = new ArrayList<>();
	private int position;

	private static final Map<String, TokenType> mapper = new HashMap<>();

	static {
		mapper.put(":", TokenType.COLON);
		mapper.put(";", TokenType.SEMICOLON);
		mapper.put(",", TokenType.COMMA);
		mapper.put("(", TokenType.OPEN_PARENTHESES);
		mapper.put(")", TokenType.CLOSED_PARENTHESES);
		mapper.put("<=", TokenType.ASSIGN);
		mapper.put(".", TokenType.DOT);
		mapper.put("=>", TokenType.MAP);
	}

	private static final Set<String> operators = new HashSet<>();

	static {
		operators.add("and");
		operators.add("or");
		operators.add("xor");
		operators.add("nand");
		operators.add("nor");
		operators.add("not");
		operators.add("xnor");
		operators.add("&");
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
		keywords.add("signal");
		keywords.add("std_logic_vector");
		keywords.add("to");
		keywords.add("downto");
		keywords.add("work");
		keywords.add("map");
		keywords.add("open");
	}

	public Lexer(String program) {
		data = program.toCharArray();
		extractNextToken();
	}

	public Token getCurrentToken() {
		return tokens.get(position);
	}

	public Token nextToken() {
		if (position == tokens.size() - 1) {
			extractNextToken();
		}

		position++;
		return getCurrentToken();
	}

	public void seek(Token token) {
		if (tokens.contains(token)) {
			position = tokens.indexOf(token);
		}
	}

	private void extractNextToken() {
		if (position != 0 && tokens.get(position).getType() == TokenType.EOF) {
			throw new LexerException("No tokens available.");
		}

		skipBlanks();

		if (currPos >= data.length) {
			tokens.add(new Token(TokenType.EOF, null));
			return;
		}

		if (Character.isDigit(data[currPos])) {
			scanNumber();
			return;
		}

		if (Character.isLetter(data[currPos]) || data[currPos] == '&') {
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

	private void scanNumber() {
		int start = currPos;

		while (currPos < data.length && Character.isDigit(data[currPos])) {
			currPos++;
		}

		int end = currPos;

		int number = Integer.parseInt(new String(data, start, end - start));
		tokens.add(new Token(TokenType.NUMBER, number));
	}

	private void scanConstant() {
		//skipping '
		char startChar = data[currPos++];

		if (startChar == '\'') {
			LogicValue value = LogicValue.getValue(data[currPos++]);

			//reading '
			if (data[currPos++] != '\'') {
				throw new LexerException("Constant not closed.");
			}

			tokens.add(new Token(TokenType.CONSTANT, value));
			return;
		}

		//vector constant
		int start = currPos;
		while (currPos < data.length && data[currPos] != '"') {
			currPos++;
		}
		int end = currPos;

		LogicValue[] values = new LogicValue[end - start];
		for (int i = 0; i + start < end; i++) {
			values[i] = LogicValue.getValue(data[i + start]);
		}
		tokens.add(new Token(TokenType.CONSTANT_VECTOR, new Vector(values)));

		//skipping "
		currPos++;
	}

	private boolean isLogicValue(char c) {
		if (c == '0' || c == '1' || c == 'u' || c == 'U') {
			return true;
		}

		return false;
	}

	private boolean isConstant(char c) {
		return c == '\'' || c == '"';

	}

	private void scanMapped() {
		String s = Character.toString(data[currPos++]);

		if (mapper.containsKey(s)) {
			tokens.add(new Token(mapper.get(s), null));
			return;
		}

		//then it's <= or =>
		tokens.add(new Token(mapper.get(s + data[currPos++]), null));
	}

	private boolean isMapped(char c) {
		String s = Character.toString(c);

		if (mapper.containsKey(s)) {
			return true;
		}

		//check for <= or =>
		int tempPos = currPos + 1;
		if (tempPos < data.length && c == '<' && data[tempPos] == '=') {
			return true;
		}
		if (tempPos < data.length && c == '=' && data[tempPos] == '>') {
			return true;
		}

		return false;
	}

	private void scanIdent() {
		int startIndex = currPos;

		if (data[currPos++] == '&') {
			tokens.add(new Token(TokenType.OPERATORS, "&"));
			return;
		}

		while (currPos < data.length && isWordCharacter(data[currPos])) {
			currPos++;
		}

		int endIndex = currPos;
		String value = new String(data, startIndex, endIndex - startIndex);

		if (keywords.contains(value.toLowerCase())) {
			tokens.add(new Token(TokenType.KEYWORD, value.toLowerCase()));
			return;
		}

		if (operators.contains(value.toLowerCase())) {
			tokens.add(new Token(TokenType.OPERATORS, value.toLowerCase()));
			return;
		}

		tokens.add(new Token(TokenType.IDENT, value));
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
		if (Character.isLetter(c)) {
			return true;
		}
		if (Character.isDigit(c)) {
			return true;
		}
		if (c == '_') {
			return true;
		}

		return false;
	}
}
