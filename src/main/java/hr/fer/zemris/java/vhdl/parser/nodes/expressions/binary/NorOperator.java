package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

/**
 * Created by Dominik on 29.7.2016..
 */
public class NorOperator extends BinaryOperator {
	private static final LogicValue[][] values = {
			{ LogicValue.ONE, LogicValue.ZERO, LogicValue.UNINITIALIZED },
			{ LogicValue.ZERO, LogicValue.ZERO, LogicValue.ZERO },
			{ LogicValue.UNINITIALIZED, LogicValue.ZERO, LogicValue.UNINITIALIZED } };

	public NorOperator(
			Expression first, Expression second) {
		super(first, second);
	}

	@Override
	public LogicValue[] evaluate(Model model) {
		return calculate(values, model);
	}
}
