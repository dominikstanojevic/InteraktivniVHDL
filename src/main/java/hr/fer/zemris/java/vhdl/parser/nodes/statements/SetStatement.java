package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public class SetStatement extends Statement {
	private Signal signal;
	private Value tempValue;
	private Expression expression;

	public SetStatement(
			String id, Signal signal, Expression expression, Set<Signal> sensitivity) {
		super(id, sensitivity);

		Objects.requireNonNull(signal, "Signal cannot be null");
		Objects.requireNonNull(expression, "Expression cannot be null.");


		this.signal = signal;
		this.expression = expression;
	}

	public SetStatement(Signal signal, Expression expression, Set<Signal> sensitivity) {
		this(null, signal, expression, sensitivity);
	}



	@Override
	public void execute() {
		tempValue = expression.evaluate();
	}

	@Override
	public Signal assign() {
		if (tempValue.equals(signal.getValue())) {
			return null;
		}

		signal.setValue(tempValue);
		return signal;
	}
}
