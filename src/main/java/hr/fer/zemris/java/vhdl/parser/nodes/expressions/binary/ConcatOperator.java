package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

/**
 * Created by Dominik on 31.8.2016..
 */
public class ConcatOperator extends BinaryOperator {
	public ConcatOperator(
			Expression first, Expression second) {
		super(first, second);
	}

	@Override
	public Value evaluate(
			Table table, String label) {
		Value first = this.first.evaluate(table, label);
		Value second = this.second.evaluate(table, label);

		return Vector.concat(first, second);
	}
}
