package hr.fer.zemris.java.vhdl.models.mappers;

import java.util.List;
import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class PositionalMap extends EntityMap {
	private List<String> signals;

	public PositionalMap(String label, String entity, List<String> signals) {
		super(label, entity);

		Objects.requireNonNull(signals, "List of mapped signals cannot be null.");
		this.signals = signals;
	}

	public List<String> getSignals() {
		return signals;
	}

	@Override
	public int mapSize() {
		return signals.size();
	}
}
