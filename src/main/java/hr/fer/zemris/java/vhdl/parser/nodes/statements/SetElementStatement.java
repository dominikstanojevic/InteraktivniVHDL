package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.models.declarable.Declarable;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public class SetElementStatement extends SetStatement {
	private int position;

	public SetElementStatement(
			String id, Declarable declarable, Expression expression, Set<Declarable> sensitivity,
			int position, long delay) {
		super(id, declarable, expression, sensitivity, delay);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}
}
