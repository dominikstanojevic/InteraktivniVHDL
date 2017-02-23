package hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary;

import hr.fer.zemris.java.vhdl.hierarchy.Memory;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominik on 29.7.2016..
 */
public class NotOperator extends UnaryOperator {
    private static final Map<LogicValue, LogicValue> negation = new HashMap<>();

    static {
        negation.put(LogicValue.ZERO, LogicValue.ONE);
        negation.put(LogicValue.ONE, LogicValue.ZERO);
        negation.put(LogicValue.UNINITIALIZED, LogicValue.UNINITIALIZED);
    }

    public NotOperator(Expression expression) {
        super(expression);
    }

    @Override
    public LogicValue[] evaluate(Memory memory) {
        LogicValue[] value = expression.evaluate(memory);

        LogicValue[] result = new LogicValue[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = NotOperator.negation.get(value[i]);
        }

        return value;
    }

    @Override
    public Declaration getDeclaration() {
        return expression.getDeclaration();
    }

    @Override
    public Expression prepareExpression(Model model) {
        return new NotOperator(expression.prepareExpression(model));
    }
}
