package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;

/**
 * Created by Dominik on 26.7.2016..
 */
public class TestParser {

	public static void main(String[] args) {
		String program = "entity majority IS port ( A, B, C: in std_logic;\n\t\tY:out "
						 + "std_logic\n);end majority;\n\nARCHITECTURE concurrent of "
						 + "majority is\n\nbegin Y<=(A or '1') or (B or A) and (C and A);"
						 + "\nend concurrent;";

		Lexer lexer = new Lexer(program);
		new Parser(lexer);
	}
}
