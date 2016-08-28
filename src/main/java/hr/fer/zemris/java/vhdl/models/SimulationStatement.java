package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.models.declarable.Declarable;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetElementStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Dominik on 23.8.2016..
 */
public class SimulationStatement {
	private String component;
	private Value tempValue;
	private SetStatement statement;

	public SimulationStatement(
			String component, SetStatement statement) {
		this.component = component;
		this.statement = statement;
	}

	public boolean sensitiveForSignals(Table table, List<String> signals) {
		Set<Declarable> sensitivity = statement.getSensitivity();

		Optional<Declarable> result = sensitivity.stream()
				.filter(s -> signals.contains(table.getSignal(component, s.getName()).getName()))
				.findFirst();

		return result.isPresent();
	}

	public boolean execute(Table table) {
		tempValue = statement.getExpression().evaluate(table, component);

		return !tempValue.equals(table.getValueForSignal(component, statement.getDeclarable
				().getName()));
	}

	public void assign(Table table) {
		Value value = table.getValueForSignal(component, statement.getDeclarable().getName());

		if (statement instanceof SetElementStatement) {
			((Vector) value).setLogicValue((LogicValue) tempValue,
					((SetElementStatement) statement).getPosition());
		} else {
			table.setValueForSignal(component, statement.getDeclarable().getName(), tempValue);
		}
	}

	public Signal getSignal(Table table) {
		return table.getSignal(component, statement.getDeclarable().getName());
	}
}
