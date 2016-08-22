package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode implements INode {
	private String name;
	private List<Signal> internalSignals;
	private List<Statement> statements;

	public ArchitectureNode(String name) {
		this.name = name;
	}

	public ArchitectureNode(
			String name, List<Signal> internalSignals, List<Statement> statements) {
		this.name = name;
		this.internalSignals = internalSignals;
		this.statements = statements;
	}

	public List<Statement> getStatements() {
		return statements;
	}

	public void addStatement(Statement statement) {
		if(statements == null) {
			statements = new ArrayList<>();
		}

		statements.add(statement);
	}

	public void addSignals(List<Signal> signals) {
		if(internalSignals == null) {
			internalSignals = new ArrayList<>();
		}

		internalSignals.addAll(signals);
	}

	public String getName() {
		return name;
	}

	public List<Statement> getStatementForSignal(Signal signal) {
		return statements.stream().filter(s -> s.containsSignal(signal)).collect(Collectors
				.toList());
	}
}
