package hr.fer.zemris.java.vhdl.models.components;

import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.declarable.Port;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.declarations.PortDeclaration;

import java.util.ArrayList;
import java.util.Collections;
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
	private Component parent;
	private List<Component> children;

	public Component(
			String label, Set<Port> ports, Set<Signal> signals,
			List<SimulationStatement> statements, Component parent) {
		this.label = label;
		this.ports = ports;
		this.signals = signals;
		this.statements = statements;
		this.parent = parent;
	}

	public Component getParent() {
		return parent;
	}

	public void addChildren(List<Component> children) {
		if (children == null) {
			this.children = Collections.EMPTY_LIST;
		} else {
			this.children = children;
		}
	}

	private Signal getSignalFromPort(Port port) {
		return parent.signals.stream().filter(s -> s.getName().equals("/" + port.getName()))
				.findFirst().get();
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

	public int numberOfInputs() {
		return ports.stream()
				.filter(p -> p.getDeclaration().getPortType() == PortDeclaration.Type.IN)
				.mapToInt(p -> p.getDeclaration().size()).sum();
	}

	public int numberOfOutputs() {
		return ports.stream()
				.filter(p -> p.getDeclaration().getPortType() == PortDeclaration.Type.OUT)
				.mapToInt(p -> p.getDeclaration().size()).sum();
	}

	public List<Signal> getInputSignals() {
		List<Signal> signals = new ArrayList<>();

		for (Port p : ports) {
			if (p.getDeclaration().getPortType() != PortDeclaration.Type.IN) {
				continue;
			}

			signals.add(getSignalFromPort(p));
		}

		return signals;
	}

	public List<Signal> getOutputSignals() {
		List<Signal> signals = new ArrayList<>();

		for (Port p : ports) {
			if (p.getDeclaration().getPortType() != PortDeclaration.Type.OUT) {
				continue;
			}

			signals.add(getSignalFromPort(p));
		}

		return signals;
	}
}
