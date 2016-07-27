package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class InputNode implements INode {
	private List<Variable> inputs;

	public InputNode(List<Variable> inputs) {
		this.inputs = inputs;
	}

	public List<Variable> getInputs() {
		return inputs;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitInputNode(this);
	}
}
