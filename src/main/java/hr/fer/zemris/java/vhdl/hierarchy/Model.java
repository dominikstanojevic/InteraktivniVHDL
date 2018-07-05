package hr.fer.zemris.java.vhdl.hierarchy;

import hr.fer.zemris.java.vhdl.environment.IModelListener;
import hr.fer.zemris.java.vhdl.environment.Table;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 24.2.2017..
 */
public class Model {
    private Memory memory;
    private Table table;
    private Component uut;
    private List<IModelListener> listeners = new ArrayList<>();

    public Model(Memory memory, Table table, Component uut) {
        this.memory = memory;
        this.table = table;
        this.uut = uut;
    }

    public void addListener(IModelListener listener) {
        listeners = new ArrayList<>(listeners);
        listeners.add(listener);
    }

    public void removeListener(IModelListener listener) {
        listeners = new ArrayList<>(listeners);
        listeners.remove(listener);
    }

    public void signalChanged(int address, LogicValue value, long time) {
        memory.write(value, address);
        //System.out.println(value + ", " + address);

        for (IModelListener listener : listeners) {
            listener.signalChanged(address, time);
        }
    }

    public LogicValue[] getValues(int[] addresses) {
        return memory.read(addresses);
    }

    public LogicValue getValue(int address) {
        return memory.read(address);
    }

    public Table getTable() {
        return table;
    }

    public Component getUut() {
        return uut;
    }

    public Memory getMemory() {
        return memory;
    }
}
