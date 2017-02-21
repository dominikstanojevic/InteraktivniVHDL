package hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.parser.ParserException;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.DeclarationExpression;

/**
 * Created by Dominik on 7.8.2016..
 */
public class IndexerOperator extends DeclarationExpression {
    private VectorData data;

    public IndexerOperator(Declaration declaration, VectorData data) {
        super(declaration);

        this.data = data;
    }

    public VectorData getData() {
        return data;
    }

    @Override
    public Value evaluate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Declaration getDeclaration() {
        if (data.isValid(declaration)) {
            if (data.getOrder() == null) {
                return new Declaration(declaration.getLabel() + data.getStart());
            } else {
                return new Declaration(declaration.getLabel() + "sub", data);
            }
        } else {
            throw new ParserException("Not valid.");
        }
    }

    @Override
    public boolean isValid() {
        return data.isValid(declaration);
    }

    @Override
    public int valueSize() {
        return data.getSize();
    }
}
