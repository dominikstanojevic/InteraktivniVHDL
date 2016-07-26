package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 26.7.2016..
 */
public class EndNode implements INode {
	private String name;

	public EndNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
