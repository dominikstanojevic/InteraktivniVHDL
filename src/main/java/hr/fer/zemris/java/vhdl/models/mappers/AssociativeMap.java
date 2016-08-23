package hr.fer.zemris.java.vhdl.models.mappers;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class AssociativeMap extends EntityMap{
	private Map<String, String> mappedSignals;

	public AssociativeMap(String label, String entity, Map<String, String> mappedSignals) {
		super(label, entity);

		Objects.requireNonNull(mappedSignals, "Mapped signals cannot be null.");
		this.mappedSignals = mappedSignals;
	}

	public boolean isMapped(String signal) {
		return mappedSignals.containsKey(signal);
	}

	public String mappedTo(String signal) {
		return mappedSignals.get(signal);
	}

	@Override
	public int mapSize() {
		return mappedSignals.size();
	}
}
