package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode implements INode {
	private String name;
	private String entity;
	private List<ExpressionNode> expressions;
	private String end;

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

	public String getName() {
		return name;
	}

	public String getEntity() {
		return entity;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitArchNode(this);
	}
}
