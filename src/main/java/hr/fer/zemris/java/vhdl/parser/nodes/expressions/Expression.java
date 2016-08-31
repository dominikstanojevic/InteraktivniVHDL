package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.values.Value;

/**
 * Created by Dominik on 29.7.2016..
 */
public abstract class Expression {
	public abstract Value evaluate(Table table, String label);
}
