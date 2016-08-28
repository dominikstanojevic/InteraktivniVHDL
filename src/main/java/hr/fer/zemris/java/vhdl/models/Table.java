package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class Table {
	private Component component;
	private Map<String, Signal> signals = new HashMap<>();
	private Map<String, String> aliases = new HashMap<>();
	private List<SimulationStatement> statements = new ArrayList<>();

	public Table(String name) {
		Objects.requireNonNull(name, "Tested entity's name cannot be null.");
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public Value getValueForSignal(String componentName, String signalName) {
		return getSignal(componentName, signalName).getValue();
	}

	public Signal getSignal(String component, String name) {
		String signal = convertSignal(component, name);

		return signals.containsKey(signal) ?
				signals.get(signal) :
				signals.get(getOriginalForAlias(component, name));
	}

	public void addSignal(Signal signal) {
		signals.put(signal.getName(), signal);
	}

	public void addAlias(String alias, String original) {
		if (aliases.containsKey(original)) {
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
		Signal signal = getSignal(componentName, signalName);
		signal.setValue(value);
	}

	public void addStatement(SimulationStatement statement) {
		statements.add(statement);
	}

	public Map<String, Signal> getSignals() {
		return signals;
	}

	public List<SimulationStatement> getStatements() {
		return statements;
	}

	public Component getTestedComponent() {
		return component.getChildren().get(0);
	}
}
