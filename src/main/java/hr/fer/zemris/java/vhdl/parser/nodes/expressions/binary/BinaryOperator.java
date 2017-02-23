package hr.fer.zemris.java.vhdl.parser.nodes.expressions.binary;

import hr.fer.zemris.java.vhdl.hierarchy.Memory;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.ParserException;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public abstract class BinaryOperator extends Expression {
    protected Expression first;
    protected Expression second;

    public BinaryOperator(Expression first, Expression second) {
        Objects.requireNonNull(first, "First expression cannot be null.");
        Objects.requireNonNull(second, "Second expression cannot be null.");

        this.first = first;
        this.second = second;
    }

    protected LogicValue[] calculate(LogicValue[][] valueTable, Memory memory) {
        LogicValue[] first = this.first.evaluate(memory);
        LogicValue[] second = this.second.evaluate(memory);

        return evaluateVector(first, second, valueTable);
    }

    protected static LogicValue[] evaluateVector(LogicValue[] first, LogicValue[] second, LogicValue[][] table) {
        if (first.length != second.length) {
            throw new RuntimeException("Expressions are not the same size.");
        }

        LogicValue[] result = new LogicValue[first.length];

        for (int i = 0; i < first.length; i++) {
            LogicValue firstValue = first[i];
            LogicValue secondValue = second[i];

            result[i] = table[firstValue.ordinal()][secondValue.ordinal()];
        }

        return result;
    }

    @Override
    public Declaration getDeclaration() {
        return first.getDeclaration();
    }

    @Override
    public boolean isValid() {
        int firstSize = first.valueSize();
        int secondSize = second.valueSize();

        return firstSize == secondSize;
    }

    @Override
    public int valueSize() {
        int firstSize = first.valueSize();
        if (firstSize != second.valueSize()) {
            throw new ParserException("First and second arugment are not of the same size.");
        }

        return firstSize;
    }

    @Override
    public Expression prepareExpression(Model model) {
        try {
            return this.getClass().getConstructor(Expression.class, Expression.class)
                    .newInstance(first.prepareExpression(model), second.prepareExpression(model));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Ovo je greška.");
        }
    }
}
