package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 27.7.2016..
 */
public class InvalidConstantException extends Exception {
	public InvalidConstantException(String c) {
		super(c + " is not a valid constant.");
	}
}
