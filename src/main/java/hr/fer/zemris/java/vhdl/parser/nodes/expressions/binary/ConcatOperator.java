package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.hierarchy.Memory;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

/**
 * Created by Dominik on 31.8.2016..
 */
public class ConcatOperator extends BinaryOperator {
    public ConcatOperator(
            Expression first, Expression second) {
        super(first, second);
    }

    @Override
    public LogicValue[] evaluate(Memory memory) {
        LogicValue[] first = this.first.evaluate(memory);
        LogicValue[] second = this.second.evaluate(memory);

        LogicValue[] result = new LogicValue[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
