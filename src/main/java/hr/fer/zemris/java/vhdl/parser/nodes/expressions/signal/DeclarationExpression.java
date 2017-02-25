package hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal;

import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.AddressExpression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mappable;

import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class DeclarationExpression extends Expression implements Mappable {
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
    public LogicValue[] evaluate(Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression prepareExpression(Component component) {
        return new AddressExpression(component.getAddresses(declaration));
    }
}
