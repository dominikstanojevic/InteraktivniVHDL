package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dominik on 25.7.2016..
 */
public class InputNode implements INode {
	private Set<VariableNode> inputs;

	public void addInput(VariableNode input) {
		if(inputs == null) {
			inputs = new HashSet<>();
		}

		inputs.add(input);
	}

	public Set<VariableNode> getInputs() {
		return inputs;
	}
}
