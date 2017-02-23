package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.hierarchy.Memory;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

/**
 * Created by Dominik on 29.7.2016..
 */
public class NandOperator extends BinaryOperator {
	private static final LogicValue[][] values = {
			{ LogicValue.ONE, LogicValue.ONE, LogicValue.ONE },
			{ LogicValue.ONE, LogicValue.ZERO, LogicValue.UNINITIALIZED },
			{ LogicValue.ONE, LogicValue.UNINITIALIZED, LogicValue.UNINITIALIZED } };

	public NandOperator(
			Expression first, Expression second) {
		super(first, second);
	}

	@Override
	public LogicValue[] evaluate(Memory memory) {
		return calculate(values, memory);
	}
}
