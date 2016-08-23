package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

/**
 * Created by Dominik on 29.7.2016..
 */
public class XorOperator extends BinaryOperator {
	private static final LogicValue[][] values = {
			{LogicValue.ZERO, LogicValue.ONE, LogicValue.UNINITIALIZED},
			{LogicValue.ONE, LogicValue.ZERO, LogicValue.UNINITIALIZED},
			{LogicValue.UNINITIALIZED, LogicValue.UNINITIALIZED, LogicValue.UNINITIALIZED}
	};

	public XorOperator(
			Expression first, Expression second) {
		super(first, second);
	}

	@Override
	public Value evaluate(Table table, String label) {
		return calculate(values, table, label);
	}
}
