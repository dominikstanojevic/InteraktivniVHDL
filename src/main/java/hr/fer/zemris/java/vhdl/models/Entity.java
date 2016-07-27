package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.executor.PreviousDeclarationException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dominik on 27.7.2016..
 */
public class Entity {
	private String name;
	private Set<String> inputs = new HashSet<>();
	private Set<String> outputs = new HashSet<>();

	public Entity(String name) {
		this.name = name;
	}

	public boolean containsInput(String input) {
		return inputs.contains(input);
	}

	public boolean containsOutput(String output) {
		return outputs.contains(output);
	}

	public void addInput(String input) throws PreviousDeclarationException {
		if(inputs.contains(input)) {
			throw new PreviousDeclarationException(input);
		}

		inputs.add(input);
	}

	public void addOutput(String output) throws PreviousDeclarationException {
		if(outputs.contains(output)) {
			throw new PreviousDeclarationException(output);
		}

		outputs.add(output);
	}

	public boolean containsVariable(String variable) {
		return containsInput(variable) || containsOutput(variable);
	}

	public Set<String> getOutputs() {
		return outputs;
	}

	public String getName() {
		return name;
	}
}
