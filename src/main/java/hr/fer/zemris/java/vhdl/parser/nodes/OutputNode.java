package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class OutputNode extends DeclarationNode {

	public OutputNode(List<Variable> variables) {
		super(variables);
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitOutputNode(this);
	}
}
