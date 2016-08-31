package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.parser.DeclarationTable;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ProgramNode {
	private EntityNode entity;
	private ArchitectureNode architecture;
	private DeclarationTable declarationTable;

	public ProgramNode(
			EntityNode entity, ArchitectureNode architecture,
			DeclarationTable declarationTable) {
		this.entity = entity;
		this.architecture = architecture;
		this.declarationTable = declarationTable;
	}

	public EntityNode getEntity() {
		return entity;
	}

	public ArchitectureNode getArchitecture() {
		return architecture;
	}

	public DeclarationTable getDeclarationTable() {
		return declarationTable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ProgramNode that = (ProgramNode) o;

		return entity.equals(that.entity);

	}

	@Override
	public int hashCode() {
		return entity.hashCode();
	}
}
