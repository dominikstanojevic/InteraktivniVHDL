package hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class DeclarationExpression extends Expression {
    protected Declaration declaration;

    public DeclarationExpression(Declaration declaration) {
        Objects.requireNonNull(declaration, "Declaration cannot be null");

        this.declaration = declaration;
    }

    @Override
    public Declaration getDeclaration() {
        return declaration;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int valueSize() {
        return declaration.getVectorData() == null ? 1 : declaration.getVectorData().getSize();
    }

    @Override
    public Value evaluate() {
        throw new UnsupportedOperationException();
    }
}
