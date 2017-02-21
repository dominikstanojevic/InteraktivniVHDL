package hr.fer.zemris.java.vhdl.models.values;

/**
 * Created by Dominik on 21.2.2017..
 */
public class Value {
    private LogicValue[] value;
    private Type type;

    public Value(LogicValue[] value, Type type) {
        this.value = value;
        this.type = type;
    }

    public LogicValue[] getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}
