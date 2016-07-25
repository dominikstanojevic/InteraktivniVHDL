package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 25.7.2016..
 */
public class VariableNode implements INode {
	private String name;

	public VariableNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
