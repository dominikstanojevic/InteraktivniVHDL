package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public class SetElementStatement extends SetStatement {
	private int position;

	public SetElementStatement(
			String id, String signal, Expression expression, Set<String> sensitivity,
			int position) {
		super(id, signal, expression, sensitivity);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}
}
