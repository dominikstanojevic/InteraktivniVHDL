package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 30.7.2016..
 */
public class DeclarationTable {
	private String entryName;
	private Map<String, Signal> signals;
	private String archName;

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public void addSignal(Signal signal) {
		if (signals == null) {
			signals = new HashMap<>();
		}

		if (signals.containsValue(signal)) {
			throw new ParserException("Signal " + signal.getId() + " already declared.");
		}

		signals.put(signal.getId(), signal);
	}

	public boolean containsSignal(String signal) {
		return signals.containsKey(signal);
	}

	public Signal getSignal(String signal) {
		return signals.get(signal);
	}

	public String getArchName() {
		return archName;
	}

	public void setArchName(String archName) {
		this.archName = archName;
	}

	public List<Signal> getInputSignals() {
		return signals.values().stream().filter(s -> s.getSignalType() == Signal.Type.IN)
				.collect(Collectors.toList());
	}

	public Collection<Signal> getSignals() {
		return signals.values();
	}
}
