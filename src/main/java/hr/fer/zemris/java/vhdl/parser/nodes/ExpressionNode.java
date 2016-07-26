package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.Queue;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ExpressionNode implements INode {
	private VariableNode variable;
	private Queue<IExpressionElement> expression;

	public ExpressionNode(VariableNode variable, Queue<IExpressionElement> expression) {
		this.variable = variable;
		this.expression = expression;
	}

	public VariableNode getVariable() {
		return variable;
	}

	public Queue<IExpressionElement> getExpression() {
		return expression;
	}
}
