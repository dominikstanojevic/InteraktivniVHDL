package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.List;

/**
 * Created by Dominik on 27.7.2016..
 */
public abstract class DeclarationNode implements INode {
	private List<Variable> variables;

	public DeclarationNode(List<Variable> variables) {
		this.variables = variables;
	}

	public List<Variable> getVariables() {
		return variables;
	}
}
