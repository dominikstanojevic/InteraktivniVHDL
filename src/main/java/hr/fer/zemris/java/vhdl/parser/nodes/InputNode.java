package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class InputNode implements INode {
	private List<VariableNode> inputs;

	public InputNode(List<VariableNode> inputs) {
		this.inputs = inputs;
	}

	public List<VariableNode> getInputs() {
		return inputs;
	}
}
