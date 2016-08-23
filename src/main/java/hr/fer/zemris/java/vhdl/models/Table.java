package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class Table {
	private String testedEntity;
	private Map<String, ProgramNode> components = new HashMap<>();
	private Map<String, Value> signals = new HashMap<>();
	private Map<String, String> aliases = new HashMap<>();
	private List<SimulationStatement> statements = new ArrayList<>();

	public Table(String name) {
		Objects.requireNonNull(name, "Tested entity's name cannot be null.");

		this.testedEntity = name;
	}

	public Value getValueForSignal(String componentName, String signalName) {
		Value value = signals.get(convertSignal(componentName, signalName));

		if(value != null) {
			return value;
		}

		return signals.get(aliases.get(convertSignal(componentName, signalName)));
	}

	public String getSignal(String component, String name) {
		String signal = convertSignal(component, name);

		return signals.containsKey(signal) ? signal : getOriginalForAlias(component, name);
	}

	public void addComponent(String name, ProgramNode component) {
		Objects.requireNonNull(name, "Component name must not be null.");
		Objects.requireNonNull(component, "Component cannot be null.");

		components.put(name, component);
	}

	public boolean containsComponent(String name) {
		return components.containsKey(name);
	}

	public ProgramNode getComponent(String name) {
		return components.get(name);
	}

	public void addSignal(String name, Value value) {
		signals.put(name, value);
	}

	public void addAlias(String alias, String original) {
		if(aliases.containsKey(original)) {
			aliases.put(alias, aliases.get(original));
		} else {
			aliases.put(alias, original);
		}
	}


	private String getOriginalForAlias(String component, String alias) {
		return aliases.get(convertSignal(component, alias));
	}

	public String convertSignal(String componentName, String signal) {
		return componentName + "/" + signal;
	}

	public void setValueForSignal(String componentName, String signalName, Value value) {
		String signal = getSignal(componentName, signalName);

		signals.put(signal, value);
	}

	public void addStatement(SimulationStatement statement) {
		statements.add(statement);
	}

	public Map<String, Value> getSignals() {
		return signals;
	}

	public List<SimulationStatement> getStatements() {
		return statements;
	}
}
