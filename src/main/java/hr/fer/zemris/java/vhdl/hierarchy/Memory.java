package hr.fer.zemris.java.vhdl.hierarchy;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.VectorData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 22.2.2017..
 */
public class Memory {
    private List<LogicValue> values = new ArrayList<>();

    public void write(Value value) {
        //TODO
    }

    public LogicValue[] read(VectorData address) {
        //TODO
        return null;
    }

    public int define(int size) {
        if(size < 1) {
            throw new RuntimeException("Cannot create memory word which size is less than one.");
        }

        int address = values.size();
        for(int i = address, end = address + size; i < end; i++) {
            values.add(LogicValue.UNINITIALIZED);
        }

        return address;
    }
}
