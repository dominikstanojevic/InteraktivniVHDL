package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 27.7.2016..
 */
public class OperatorNode implements INode, IExpressionElement {
	private String name;

	public OperatorNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
