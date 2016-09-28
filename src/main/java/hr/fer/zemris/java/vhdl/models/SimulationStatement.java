package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.models.declarable.Declarable;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetElementStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Dominik on 23.8.2016..
 */
public class SimulationStatement {
	private String component;
	private Value tempValue;
	private SetStatement statement;
	private boolean isOpen;

	public SimulationStatement(
			String component, SetStatement statement, Table table) {
		this.component = component;
		this.statement = statement;

		isOpen = !table.containsSignal(component, statement.getDeclarable().getName());
	}

	public boolean sensitiveForSignal(Table table, Signal signal) {
		Set<Declarable> sensitivity = statement.getSensitivity();

		Optional<Declarable> result = sensitivity.stream()
				.filter(s -> signal.equals((table.getSignal(component, s.getName()))))
				.findFirst();

		return result.isPresent();
	}

	public Value execute(Table table) {
		return statement.getExpression().evaluate(table, component);
	}

	public Signal getSignal(Table table) {
		if (isOpen) {
			return null;
		}

		return table.getSignal(component, statement.getDeclarable().getName());
	}

	public Integer getPosition(Table table) {

		if(statement instanceof SetElementStatement) {
			int position = ((SetElementStatement) statement).getPosition();
			int offset = table.getSignal(component, statement.getDeclarable().getName())
					.getDeclaration().getStart() - statement.getDeclarable().getDeclaration
					().getStart();

			return position + offset;
		} else {
			return table.aliasPosition(component, statement.getDeclarable().getName());
		}
	}

	public long getDelay() {
		return statement.getDelay();
	}
}
