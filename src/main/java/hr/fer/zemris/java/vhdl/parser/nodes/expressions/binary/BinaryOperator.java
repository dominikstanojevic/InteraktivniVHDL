package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public abstract class BinaryOperator extends Expression {
	protected Expression first;
	protected Expression second;

	public BinaryOperator(Expression first, Expression second) {
		Objects.requireNonNull(first, "First expression cannot be null.");
		Objects.requireNonNull(second, "Second expression cannot be null.");

		this.first = first;
		this.second = second;
	}

	protected Value calculate(LogicValue[][] valueTable, Table table, String label) {
		Value first = this.first.evaluate(table, label);
		Value second = this.second.evaluate(table, label);

		if (first instanceof LogicValue && second instanceof LogicValue) {
			return valueTable[((LogicValue) first).ordinal()][((LogicValue) second).ordinal()];
		} else if (first instanceof Vector && second instanceof Vector) {
			return evaluateVector((Vector) first, (Vector) second, valueTable);
		}

		throw new RuntimeException(
				"Operation for type: " + first.getClass() + " and " + second.getClass()
				+ " is not " + "supported.");
	}

	protected static Vector evaluateVector(Vector first, Vector second, LogicValue[][] table) {
		if (first.length() != second.length()) {
			throw new RuntimeException("Vectors are not the same size.");
		}

		int length = first.length();
		LogicValue[] firstOperand = first.getValue();
		LogicValue[] secondOperand = second.getValue();
		LogicValue[] result = new LogicValue[length];

		for (int i = 0; i < length; i++) {
			LogicValue firstValue = firstOperand[i];
			LogicValue secondValue = secondOperand[i];

			result[i] = table[firstValue.ordinal()][secondValue.ordinal()];
		}

		return new Vector(result);
	}

	@Override
	public Declaration getDeclaration() {
		return first.getDeclaration();
	}
}
