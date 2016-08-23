package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalDeclaration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Dominik on 25.7.2016..
 */
public class EntityNode  {
	private String name;
	private Map<String, SignalDeclaration> declarations;

	public EntityNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addSignals(Map<String, SignalDeclaration> declarations) {
		if(this.declarations == null) {
			this.declarations = new LinkedHashMap<>();
		}

		this.declarations.putAll(declarations);
	}

	public int numberOfSignals() {
		return declarations.size();
	}

	public Map<String, SignalDeclaration> getDeclarations() {
		return declarations;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		EntityNode that = (EntityNode) o;

		return name.equals(that.name);

	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
