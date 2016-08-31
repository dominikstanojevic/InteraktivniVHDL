package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.mappers.EntityMap;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode {
	private String name;
	private Set<Signal> signals;
	private List<SetStatement> statements = new ArrayList<>();
	private List<EntityMap> mappedEntities = new ArrayList<>();

	public ArchitectureNode(String name) {
		this.name = name;
	}

	public ArchitectureNode(
			String name, Set<Signal> internalSignals) {
		this.name = name;
		this.signals = internalSignals;
	}

	public List<SetStatement> getStatements() {
		return statements;
	}

	public void addStatement(Statement statement) {
		if (statement instanceof EntityMap) {
			mappedEntities.add((EntityMap) statement);
		} else {
			statements.add((SetStatement) statement);
		}
	}

	public void addSignals(Set<Signal> signals) {
		if (this.signals == null) {
			this.signals = new LinkedHashSet<>();
		}

		this.signals.addAll(signals);
	}

	public Set<Signal> getSignals() {
		return signals == null ? Collections.EMPTY_SET : signals;
	}

	public List<EntityMap> getMappedEntities() {
		return mappedEntities;
	}

	public String getName() {
		return name;
	}
}
