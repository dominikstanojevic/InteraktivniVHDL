package hr.fer.zemris.java.vhdl.models.mappers;

import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public abstract class EntityMap extends Statement{
	private String entity;

	public EntityMap(String label, String entity) {
		super(label);

		Objects.requireNonNull(label, "Port map statement must provide label.");
		Objects.requireNonNull(entity, "Entity name must be provided.");

		this.entity = entity;
	}

	public String getEntity() {
		return entity;
	}

	public abstract int mapSize();
}
