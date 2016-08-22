package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ProgramNode implements INode {
	private EntityNode entity;
	private ArchitectureNode architecture;

	public EntityNode getEntity() {
		return entity;
	}

	public void setEntity(EntityNode entity) {
		this.entity = entity;
	}

	public ArchitectureNode getArchitecture() {
		return architecture;
	}



	public void setArchitecture(ArchitectureNode architecture) {
		this.architecture = architecture;
	}
}
