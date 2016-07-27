package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class OutputNode implements INode {
	private List<Variable> outputs;

	public OutputNode(List<Variable> outputs) {
		this.outputs = outputs;
	}

	public List<Variable> getOutputs() {
		return outputs;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitOutputNode(this);
	}
}
