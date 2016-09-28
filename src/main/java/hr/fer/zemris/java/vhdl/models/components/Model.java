package hr.fer.zemris.java.vhdl.models.components;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

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

	public void signalChange(String signal, Value value, Integer position, long time) {
		Signal s = table.getSignal("", signal);
		signalChange(s, value, position, time);
	}

	public void signalChange(Signal signal, Value value, Integer position, long time) {
		setValue(signal, value, position);


		for(IModelListener listener : listeners) {
			listener.signalChanged(signal, time);
		}
	}

	private void setValue(Signal signal, Value value, Integer position) {
		if (position != null) {
			((Vector) signal.getValue()).setLogicValue((LogicValue) value, position);
		} else {
			signal.setValue(value);
		}
	}

	public Table getTable() {
		return table;
	}

	public Component getComponent() {
		return component;
	}
}
