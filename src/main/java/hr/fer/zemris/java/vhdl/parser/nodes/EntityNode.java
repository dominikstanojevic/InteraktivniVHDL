package hr.fer.zemris.java.vhdl.parser.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class EntityNode implements INode {
	private String name;
	private List<InputNode> inputs;
	private List<OutputNode> outputs;
	private EndNode end;

	public EntityNode(String name) {
		this.name = name;
	}

	public void addInput(InputNode input) {
		if(inputs == null) {
			inputs = new ArrayList<>();
		}

		inputs.add(input);
	}

	public void addOutput(OutputNode output) {
		if(outputs == null) {
			outputs = new ArrayList<>();
		}

		outputs.add(output);
	}

	public String getName() {
		return name;
	}

	public EndNode getEnd() {
		return end;
	}

	public void setEnd(EndNode end) {
		this.end = end;
	}

	public List<InputNode> getInputs() {
		return inputs;
	}

	public List<OutputNode> getOutputs() {
		return outputs;
	}
}
