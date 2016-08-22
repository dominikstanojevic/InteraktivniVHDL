package hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

/**
 * Created by Dominik on 29.7.2016..
 */
public class NotOperator extends UnaryOperator {
	private static LogicValue[] values = new LogicValue[] {
			LogicValue.ONE, LogicValue.ZERO, LogicValue.UNINITIALIZED };

	public NotOperator(Expression expression) {
		super(expression);
	}

	@Override
	public Value evaluate() {
		Value value = expression.evaluate();

		if (value instanceof LogicValue) {
			return values[((LogicValue) value).ordinal()];
		} else {
			return evaluateVector((Vector) value);
		}
	}

	private Value evaluateVector(Vector vector) {
		LogicValue[] values = vector.getValue();
		LogicValue[] result = new LogicValue[values.length];

		for (int i = 0; i < values.length; i++) {
			result[i] = this.values[values[i].ordinal()];
		}

		return new Vector(result);
	}
}
