package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary.AndOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary.ConcatOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary.NandOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary.NorOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary.OrOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary.XnorOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary.XorOperator;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary.NotOperator;

/**
 * Created by Dominik on 29.7.2016..
 */
public class OperatorFactory {
	public static Expression getUnaryOperator(Expression first, String name) {
		switch (name) {
			case "not":
				return new NotOperator(first);
			default:
				throw new IllegalArgumentException(
						"No unary operator named " + name + " " + "exists.");
		}
	}

	public static Expression getBinaryOperator(
			Expression first, Expression second, String name) {
		switch (name) {
			case "and":
				return new AndOperator(first, second);
			case "nand":
				return new NandOperator(first, second);
			case "or":
				return new OrOperator(first, second);
			case "nor":
				return new NorOperator(first, second);
			case "xor":
				return new XorOperator(first, second);
			case "xnor":
				return new XnorOperator(first, second);
			case "&":
				return new ConcatOperator(second, first);
			default:
				throw new IllegalArgumentException(
						"No binary operator named " + name + " " + "exists.");
		}
	}
}
