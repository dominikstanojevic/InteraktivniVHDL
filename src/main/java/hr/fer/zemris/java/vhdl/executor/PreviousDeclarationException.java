package hr.fer.zemris.java.vhdl.executor;

/**
 * Created by Dominik on 27.7.2016..
 */
public class PreviousDeclarationException extends Exception {
	public PreviousDeclarationException(String variable) {
		super("Previous declaration of port: " + variable);
	}
}
