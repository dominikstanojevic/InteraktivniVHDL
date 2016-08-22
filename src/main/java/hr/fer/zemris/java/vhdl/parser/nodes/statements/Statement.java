package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public abstract class Statement {
	private String id;
	private Set<Signal> sensitivity;

	public Statement(String id, Set<Signal> sensitivity) {
		this.id = id;

		Objects.requireNonNull(sensitivity, "Sensitivity list cannot be null");
		this.sensitivity = sensitivity;
	}

	public abstract void execute();
	public abstract Signal assign();

	public boolean containsSignal(Signal s) {
		return sensitivity.contains(s);
	}
}
