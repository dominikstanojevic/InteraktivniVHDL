package hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary;

import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.parser.ParserException;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.AddressExpression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.DeclarationExpression;

/**
 * Created by Dominik on 7.8.2016..
 */
public class IndexerOperator extends DeclarationExpression {
    private VectorData data;
    private Declaration declaration;

    public IndexerOperator(Declaration declaration, VectorData data) {
        super(declaration);

        this.data = data;
        if (data.isValid(declaration.getVectorData())) {
            this.declaration = new Declaration(declaration.getLabel(), data);
        } else {
            throw new ParserException("Not valid.");
        }
    }

    public VectorData getData() {
        return data;
    }

    @Override
    public LogicValue[] evaluate(Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Declaration getDeclaration() {
        return declaration;
    }

    @Override
    public boolean isValid() {
        return data.isValid(declaration.getVectorData());
    }

    @Override
    public int valueSize() {
        return data.getSize();
    }

    @Override
    public Expression prepareExpression(Component component) {
        Integer[] address = component.getAddresses(getDeclaration());
        return new AddressExpression(address);
    }
}
