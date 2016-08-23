package hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class SignalExpression extends Expression {
	private String id;

	public SignalExpression(String id) {
		Objects.requireNonNull(id, "Signal id cannot be null");

		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public Value evaluate(Table table, String componentName) {
		return table.getValueForSignal(componentName, id);
	}

}
