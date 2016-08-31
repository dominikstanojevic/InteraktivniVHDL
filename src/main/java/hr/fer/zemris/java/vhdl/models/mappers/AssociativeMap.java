package hr.fer.zemris.java.vhdl.models.mappers;

import hr.fer.zemris.java.vhdl.models.declarable.Port;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class AssociativeMap extends EntityMap {
	private Map<String, Mappable> mappedSignals;

	public AssociativeMap(String label, String entity, Map<String, Mappable> mappedSignals) {
		super(label, entity);

		Objects.requireNonNull(mappedSignals, "Mapped signals cannot be null.");
		this.mappedSignals = mappedSignals;
	}

	public boolean isMapped(Port port) {
		return mappedSignals.containsKey(port.getName());
	}

	public Mappable mappedTo(Port port) {
		return mappedSignals.get(port.getName());
	}

	@Override
	public int mapSize() {
		return mappedSignals.size();
	}
}
