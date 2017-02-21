package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.ParserException;
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

	protected Value calculate(LogicValue[][] valueTable) {
		//TODO FIX
		/*Value first = this.first.evaluate();
		Value second = this.second.evaluate();

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

		return new Vector(result);*/
		return new Value(null, null);
	}

	@Override
	public Declaration getDeclaration() {
		return first.getDeclaration();
	}

	@Override
	public boolean isValid() {
		int firstSize = first.valueSize();
		int secondSize = second.valueSize();

		return firstSize == secondSize;
	}

	@Override
	public int valueSize() {
		int firstSize = first.valueSize();
		if(firstSize != second.valueSize()) {
			throw new ParserException("First and second arugment are not of the same size.");
		}

		return firstSize;
	}
}
