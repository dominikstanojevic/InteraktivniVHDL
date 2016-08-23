package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public class SetStatement extends Statement {
	private String signal;
	private Expression expression;
	private Set<String> sensitivity;

	public SetStatement(
			String label, String signal, Expression expression, Set<String> sensitivity) {
		super(label);

		Objects.requireNonNull(signal, "SignalDeclaration cannot be null");
		Objects.requireNonNull(expression, "Expression cannot be null.");

		this.signal = signal;
		this.expression = expression;
		this.sensitivity = sensitivity;
	}

	public String getSignal() {
		return signal;
	}

	public Expression getExpression() {
		return expression;
	}

	public Set<String> getSensitivity() {
		return sensitivity;
	}
}
