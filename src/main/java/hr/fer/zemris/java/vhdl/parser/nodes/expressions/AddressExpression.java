package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;

/**
 * Created by Dominik on 22.2.2017..
 */
public class AddressExpression extends Expression {
    private Integer[] address;

    public AddressExpression(Integer[] address) {
        this.address = address;
    }

    @Override
    public LogicValue[] evaluate(Model model) {
        LogicValue[] values = new LogicValue[address.length];
        for(int i = 0; i < values.length; i++) {
            values[i] = model.getValue(address[i]);
        }

        return values;
    }

    @Override
    public Declaration getDeclaration() {
        throw new UnsupportedOperationException("Cannot get declaration for address expression.");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int valueSize() {
        return address.length;
    }

    @Override
    public Expression prepareExpression(Component component) {
        return null;
    }
}
