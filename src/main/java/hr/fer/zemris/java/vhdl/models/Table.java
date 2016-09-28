package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.models.components.Component;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 22.8.2016..
 */
public class Table {
	private Component component;
	private Map<String, Signal> signals = new HashMap<>();
	private Map<String, Alias> aliases = new HashMap<>();
	private List<SimulationStatement> statements = new ArrayList<>();

	public void setComponent(Component component) {
		this.component = component;
	}

	public Value getValueForSignal(String componentName, String signalName) {
		String signal = convertSignal(componentName, signalName);

		if (signals.containsKey(signal)) {
			return signals.get(signal).getValue();
		}

		Alias alias = aliases.get(signal);
		if (alias.getPosition() == null) {
			return signals.get(alias.getOriginal()).getValue();
		} else {
			return ((Vector) signals.get(alias.getOriginal()).getValue())
					.getLogicValue(alias.getPosition());
		}
	}

	public boolean containsSignal(String component, String name) {
		String signal = convertSignal(component, name);

		return signals.containsKey(signal) || aliases.get(signal) != null;
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

	public void addAlias(String alias, String original, Integer position) {
		if (aliases.containsKey(original)) {
			aliases.put(alias, new Alias(aliases.get(original).getOriginal(), position));
		} else {
			aliases.put(alias, new Alias(original, position));
		}
	}

	private String getOriginalForAlias(String component, String alias) {
		return aliases.get(convertSignal(component, alias)).getOriginal();
	}

	public String convertSignal(String componentName, String signal) {
		return componentName + "/" + signal;
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

	public List<SimulationStatement> getStatementsForSignal(Signal signal) {
		return statements.stream().filter(s -> s.sensitiveForSignal(this, signal))
				.collect(Collectors.toList());
	}

	public Integer aliasPosition(String component, String name) {
		return aliases.get(convertSignal(component, name)).getPosition();
	}
}
