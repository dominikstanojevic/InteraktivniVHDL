package hr.fer.zemris.java.vhdl.models.components;

import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.declarable.Port;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;

import java.util.List;
import java.util.Set;

/**
 * Created by Dominik on 27.8.2016..
 */
public class Component {
	private String label;
	private Set<Port> ports;
	private Set<Signal> signals;
	private List<SimulationStatement> statements;
	private List<Component> children;

	public Component(
			String label, Set<Port> ports, Set<Signal> signals,
			List<SimulationStatement> statements, List<Component> children) {
		this.label = label;
		this.ports = ports;
		this.signals = signals;
		this.statements = statements;
		this.children = children;
	}

	public String getLabel() {
		return label;
	}

	public Set<Port> getPorts() {
		return ports;
	}

	public Set<Signal> getSignals() {
		return signals;
	}

	public List<SimulationStatement> getStatements() {
		return statements;
	}

	public List<Component> getChildren() {
		return children;
	}
}
