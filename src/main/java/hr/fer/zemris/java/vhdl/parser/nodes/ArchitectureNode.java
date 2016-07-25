package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode implements INode {
	private ExpressionNode expression;

	public ArchitectureNode(ExpressionNode expression) {
		this.expression = expression;
	}

	public ExpressionNode getExpression() {
		return expression;
	}
}
