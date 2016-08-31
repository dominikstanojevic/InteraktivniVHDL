package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

/**
 * Created by Dominik on 29.7.2016..
 */
public class InvalidTypesException extends RuntimeException{
	public InvalidTypesException() {
		super();
	}

	public InvalidTypesException(String message) {
		super(message);
	}
}
