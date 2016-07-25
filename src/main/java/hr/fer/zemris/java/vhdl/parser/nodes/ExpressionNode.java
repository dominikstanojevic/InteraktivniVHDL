package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ExpressionNode implements INode {
	private VariableNode variable;
	private List<INode> expression;

	public ExpressionNode(VariableNode variable) {
		this.variable = variable;
	}

	public void addExpressionElement(INode element) {
		if(expression == null) {
			expression = new ArrayList<>();
		}

		expression.add(element);
	}

	public VariableNode getVariable() {
		return variable;
	}

	public List<INode> getExpression() {
		return expression;
	}
}
