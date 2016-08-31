package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.lexer.Lexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Dominik on 26.7.2016..
 */
public class TestParser {

	public static void main(String[] args) throws IOException {

		String program = new String(Files.readAllBytes(Paths.get("test.txt")),
				StandardCharsets.UTF_8);
		
		Lexer lexer = new Lexer(program);
		new Parser(lexer);
	}
}
