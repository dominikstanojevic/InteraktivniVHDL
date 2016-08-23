package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.values.Value;

/**
 * Created by Dominik on 29.7.2016..
 */
public class Constant extends Expression {
	private Value value;

	public Constant(Value value) {
		this.value = value;
	}

	@Override
	public Value evaluate(Table table, String label) {
		return value;
	}
}
