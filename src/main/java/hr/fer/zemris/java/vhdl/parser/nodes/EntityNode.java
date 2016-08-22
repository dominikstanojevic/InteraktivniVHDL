package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class EntityNode  {
	private String name;
	private List<Signal> inputs;
	private List<Signal> outputs;

	public EntityNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addSignals(List<Signal> signals, Signal.Type type) {
		if(type == Signal.Type.IN) {
			addInputs(signals);
		} else if (type == Signal.Type.OUT) {
			addOutputs(signals);
		}
	}

	private void addInputs(List<Signal> signals) {
		if(inputs == null) {
			inputs = new ArrayList<>();
		}

		inputs.addAll(signals);
	}

	private void addOutputs(List<Signal> signals) {
		if(outputs == null) {
			outputs = new ArrayList<>();
		}

		outputs.addAll(signals);
	}
}
