package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.models.declarable.Declarable;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public class SetStatement extends Statement {
	private Declarable declarable;
	private Expression expression;
	private Set<Declarable> sensitivity;
	private long delay;

	public SetStatement(
			String label, Declarable declarable, Expression expression, Set<Declarable>
			sensitivity, long delay) {
		super(label);

		Objects.requireNonNull(declarable, "PortDeclaration cannot be null");
		Objects.requireNonNull(expression, "Expression cannot be null.");

		this.declarable = declarable;
		this.expression = expression;
		this.sensitivity = sensitivity;
		this.delay = delay;
	}

	public Declarable getDeclarable() {
		return declarable;
	}

	public Expression getExpression() {
		return expression;
	}

	public Set<Declarable> getSensitivity() {
		return sensitivity;
	}

	public long getDelay() {
		return delay;
	}
}
