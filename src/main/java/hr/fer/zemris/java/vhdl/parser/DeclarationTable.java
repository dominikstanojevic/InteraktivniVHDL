package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalDeclaration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 30.7.2016..
 */
public class DeclarationTable {
	private String entryName;
	private Map<String, SignalDeclaration> signals;
	private String archName;
	private Set<String> labels = new HashSet<>();

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public void addSignal(String signal, SignalDeclaration declaration) {
		if (signals == null) {
			signals = new HashMap<>();
		}

		if (signals.containsKey(signal)) {
			throw new ParserException("SignalDeclaration " + signal + " already declared.");
		}

		signals.put(signal, declaration);
	}

	public boolean containsSignal(String signal) {
		return signals.containsKey(signal);
	}

	public SignalDeclaration getSignal(String signal) {
		return signals.get(signal);
	}

	public String getArchName() {
		return archName;
	}

	public void setArchName(String archName) {
		this.archName = archName;
	}

	public SignalDeclaration getDeclaration(String signal) {
		return signals.get(signal);
	}

	public List<String> getInputSignals() {
		return signals.entrySet().stream()
				.filter(e -> e.getValue().getSignalType() == SignalDeclaration.Type.IN)
				.map(e -> e.getKey()).collect(Collectors.toList());
	}

	public Collection<SignalDeclaration> getSignals() {
		return signals.values();
	}

	public boolean addLabel(String label) {
		if (label == null) {
			return true;
		}

		return labels.add(label);
	}
}
