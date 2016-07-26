package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class OutputNode implements INode {
	private List<VariableNode> outputs;

	public OutputNode(List<VariableNode> outputs) {
		this.outputs = outputs;
	}

	public List<VariableNode> getOutputs() {
		return outputs;
	}
}
