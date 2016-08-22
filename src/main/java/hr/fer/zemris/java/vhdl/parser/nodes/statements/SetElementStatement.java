package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary.IndexerOperator;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public class SetElementStatement extends Statement {
	private IndexerOperator indexer;
	private LogicValue tempValue;
	private Expression expression;

	public SetElementStatement(
			String id, IndexerOperator indexer, Expression expression,
			Set<Signal> sensitivity) {
		super(id, sensitivity);

		Objects.requireNonNull(indexer, "Inexer cannot be null.");
		this.indexer = indexer;

		Objects.requireNonNull(expression, "Expression cannot be null.");
		this.expression = expression;
	}

	@Override
	public void execute() {
		tempValue = (LogicValue) expression.evaluate();
	}

	@Override
	public Signal assign() {
		if (tempValue.equals(indexer.evaluate())) {
			return null;
		}

		((Vector) indexer.getSignal().getValue())
				.setLogicValue(tempValue, indexer.getPosition());
		return indexer.getSignal();
	}
}
