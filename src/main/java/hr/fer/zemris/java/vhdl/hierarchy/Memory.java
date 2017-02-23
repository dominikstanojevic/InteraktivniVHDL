package hr.fer.zemris.java.vhdl.hierarchy;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 22.2.2017..
 */
public class Memory {
    private List<LogicValue> values = new ArrayList<>();

    public void write(LogicValue[] values, int[] addresses) {
        //TODO
    }

    public LogicValue[] read(int[] addresses) {
        //TODO
        return null;
    }

    public int define(LogicValue[] init) {
        int address = values.size();
        for (int i = 0; i < init.length; i++) {
            values.add(init[i]);
        }

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
