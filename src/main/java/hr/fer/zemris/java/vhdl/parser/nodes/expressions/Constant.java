package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.hierarchy.Memory;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Type;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.models.values.VectorOrder;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mappable;

/**
 * Created by Dominik on 29.7.2016..
 */
public class Constant extends Expression implements Mappable {
    private LogicValue[] values;
    private Type type;

    public Constant(LogicValue[] values, TokenType type) {
        Type valueType;
        if (type == TokenType.CONSTANT_VECTOR) {
            valueType = Type.VECTOR_STD_LOGIC;
        } else {
            valueType = Type.STD_LOGIC;
        }

        this.values = values;
        this.type = valueType;
    }

    @Override
    public LogicValue[] evaluate(Memory memory) {
        return values;
    }

    @Override
    public Declaration getDeclaration() {
        return new Declaration("const",
                new VectorData(0, type == Type.STD_LOGIC ? null : VectorOrder.TO, values.length - 1));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int valueSize() {
        return values.length;
    }

    @Override
    public Expression prepareExpression(Model model) {
        return this;
    }

    public LogicValue[] getValues() {
        return values;
    }
}
