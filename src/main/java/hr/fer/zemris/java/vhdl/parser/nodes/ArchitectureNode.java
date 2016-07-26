package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode implements INode {
	private String name;
	private String entity;
	private List<ExpressionNode> expressions;

	public ArchitectureNode(String name, String entity) {
		this.name = name;
		this.entity = entity;
	}

	public void addExpression(ExpressionNode expression) {
		if(expressions == null) {
			expressions = new ArrayList<>();
		}

		expressions.add(expression);
	}

	public List<ExpressionNode> getExpressions() {
		return expressions;
	}
}
