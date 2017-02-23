package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.Value;

/**
 * Created by Dominik on 22.2.2017..
 */
public class AddressExpression extends Expression {
    private int[] address;

    public AddressExpression(int[] address) {
        this.address = address;
    }

    @Override
    public Value evaluate() {
        return null;
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
    public Expression prepareExpression(Model model) {
        return null;
    }
}
