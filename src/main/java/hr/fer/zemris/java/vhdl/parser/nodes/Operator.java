package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 27.7.2016..
 */
public class Operator implements IExpressionElement {
	private String name;

	public Operator(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
