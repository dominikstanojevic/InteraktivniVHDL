package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.models.mappers.EntityMap;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalDeclaration;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode {
	private String name;
	private Map<String, SignalDeclaration> signals;
	private List<SetStatement> statements = new ArrayList<>();
	private List<EntityMap> mappedEntities = new ArrayList<>();

	public ArchitectureNode(String name) {
		this.name = name;
	}

	public ArchitectureNode(
			String name, Map<String, SignalDeclaration> internalSignals) {
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

	public void addSignals(Map<String, SignalDeclaration> signals) {
		if (this.signals == null) {
			this.signals = new LinkedHashMap<>();
		}

		this.signals.putAll(signals);
	}

	public Map<String, SignalDeclaration> getSignals() {
		return signals;
	}

	public List<EntityMap> getMappedEntities() {
		return mappedEntities;
	}

	public String getName() {
		return name;
	}
}
