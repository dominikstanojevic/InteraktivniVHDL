package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.lexer.TokenType;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Type;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mappable;

/**
 * Created by Dominik on 29.7.2016..
 */
public class Constant extends Expression implements Mappable {
    private Value value;

    public Constant(LogicValue[] value, TokenType type) {
        Type valueType;
        if (type == TokenType.CONSTANT_VECTOR) {
           valueType = Type.VECTOR_STD_LOGIC;
        } else {
            valueType = Type.STD_LOGIC;
        }

        this.value = new Value(value, valueType);
    }


    public LogicValue[] getValue() {
        return value.getValue();
    }

    @Override
    public Value evaluate() {
        return value;
    }

    @Override
    public Declaration getDeclaration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int valueSize() {
        return value.getValue().length;
    }

    @Override
    public Expression prepareExpression(Model model) {
        return this;
    }
}
