package hr.fer.zemris.java.vhdl.models.components;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 23.9.2016..
 */
public class Model {
	private Table table;
	private Component component;
	private List<IModelListener> listeners = new ArrayList<>();

	public Model(Table table, Component component) {
		this.table = table;
		this.component = component;
	}

	public void addListener(IModelListener listener) {
		listeners = new ArrayList<>(listeners);
		listeners.add(listener);
	}

	public void removeListener(IModelListener listener) {
		listeners = new ArrayList<>(listeners);
		listeners.remove(listener);
	}

	public void signalChange(String signal, Value value, long time) {
		Signal s = table.getSignal("", signal);
		signalChange(s, value, time);
	}

	public void signalChange(Signal signal, Value value, long time) {
		signal.setValue(value);
		signalChange(signal, time);
	}

	public void signalChange(Signal signal, long time) {
		for(IModelListener listener : listeners) {
			listener.signalChanged(signal, time);
		}
	}

	public Table getTable() {
		return table;
	}

	public Component getComponent() {
		return component;
	}
}
