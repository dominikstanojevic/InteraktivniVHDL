package hr.fer.zemris.java.vhdl.hierarchy;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dominik on 22.2.2017..
 */
public class Memory {
    private List<LogicValue> values = new ArrayList<>();

    public void write(LogicValue[] values, int[] addresses) {
        if (values.length != addresses.length) {
            throw new RuntimeException("Number of addresses and number of values differ");
        }

        for (int i = 0; i < values.length; i++) {
            this.values.set(addresses[i], values[i]);
        }
    }

    public void write(LogicValue value, int address) {
        values.set(address, value);
    }

    public LogicValue[] read(int[] addresses) {
        LogicValue[] values = new LogicValue[addresses.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = this.values.get(addresses[i]);
        }

        return values;
    }

    public LogicValue read(int address) {
        return values.get(address);
    }

    public int define(LogicValue[] init) {
        int address = values.size();
        Collections.addAll(values, init);

        return address;
    }

    public int define(int size) {
        if (size < 1) {
            throw new RuntimeException("Cannot create memory word which size is less than one.");
        }

        int address = values.size();
        for (int i = 0; i < size; i++) {
            values.add(LogicValue.UNINITIALIZED);
        }

        return address;
    }
}
