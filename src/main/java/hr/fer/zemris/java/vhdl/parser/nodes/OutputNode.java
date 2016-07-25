package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dominik on 25.7.2016..
 */
public class OutputNode implements INode {
	private Set<VariableNode> outputs;

	public void addOutput(VariableNode output) {
		if(outputs == null) {
			outputs = new HashSet<>();
		}

		outputs.add(output);
	}

	public Set<VariableNode> getOutputs() {
		return outputs;
	}
}
