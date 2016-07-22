package hr.fer.zemris.java.vhdl.lexer;

/**
 * Created by Dominik on 22.7.2016..
 */
public class LexerTest {
	public static void main(String[] args) {
		String program = "entity majority IS port ( A, B, C: in std_logic\n\t\tY:out "
						 + "std_logic\n);end majority;\n\nARCHITECTURE concurrent of "
						 + "majority is\n\nbegin Y<=(A AND B) or (A and C) or (B and C);"
						 + "\nend conncurent;";

		Lexer lexer = new Lexer(program);

		while (true) {
			Token token = lexer.getCurrentToken();
			System.out.println(
					"Trenutni token: " + token.getType() + ", vrijednost: " + token.getValue()
					+ ".");
			if (token.getType() == TokenType.EOF) {
				break;
			}

			lexer.nextToken();
		}
	}
}
