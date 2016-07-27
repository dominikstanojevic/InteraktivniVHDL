package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

import java.util.Queue;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ExpressionNode implements INode {
	private Variable variable;
	private Queue<IExpressionElement> expression;

	public ExpressionNode(Variable variable, Queue<IExpressionElement> expression) {
		this.variable = variable;
		this.expression = expression;
	}

	public Variable getVariable() {
		return variable;
	}

	public Queue<IExpressionElement> getExpression() {
		return expression;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitExpressionNode(this);
	}
}
