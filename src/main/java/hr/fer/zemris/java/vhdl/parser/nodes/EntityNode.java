package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.models.declarable.Port;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Dominik on 25.7.2016..
 */
public class EntityNode {
	private String name;
	private Set<Port> declarations;

	public EntityNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addSignals(Set<Port> declarations) {
		if (this.declarations == null) {
			this.declarations = new LinkedHashSet<>();
		}

		this.declarations.addAll(declarations);
	}

	public int numberOfSignals() {
		return declarations.size();
	}

	public Set<Port> getDeclarations() {
		return declarations != null ? declarations : Collections.emptySet();
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
