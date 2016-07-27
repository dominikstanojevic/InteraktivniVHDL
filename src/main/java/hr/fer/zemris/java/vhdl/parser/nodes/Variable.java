package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 25.7.2016..
 */
public class Variable implements IExpressionElement {
	private String name;

	public Variable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
