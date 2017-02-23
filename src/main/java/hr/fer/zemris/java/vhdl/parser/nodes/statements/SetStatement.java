package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.DeclarationExpression;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Dominik on 9.8.2016..
 */
public class SetStatement extends Statement {
    private DeclarationExpression declarable;
    private Expression expression;
    private Set<DeclarationExpression> sensitivity;
    private long delay;

    public SetStatement(
            String label, DeclarationExpression declarable, Expression expression,
            Set<DeclarationExpression> sensitivity, long delay) {
        super(label);

        Objects.requireNonNull(declarable, "PortDeclaration cannot be null");
        Objects.requireNonNull(expression, "Expression cannot be null.");

        this.declarable = declarable;
        this.expression = expression;
        this.sensitivity = sensitivity;
        this.delay = delay;
    }

    public DeclarationExpression getDeclarable() {
        return declarable;
    }

    public Expression getExpression() {
        return expression;
    }

    public long getDelay() {
        return delay;
    }

    public AddressStatement prepareStatement(Model model) {
        Integer[] address = model.getAddresses(declarable.getDeclaration());
        Expression expression = this.expression.prepareExpression(model);
        Set<Integer> sensitivity = mapSensitivityToAddresses(model);
        return new AddressStatement(address, expression, sensitivity, delay);
    }

    private Set<Integer> mapSensitivityToAddresses(Model model) {
        Set<Integer> sensitivity = new HashSet<>();
        for (DeclarationExpression e : this.sensitivity) {
            Declaration declaration = e.getDeclaration();
            Integer[] addresses = model.getAddresses(declaration);
            sensitivity.addAll(Arrays.asList(addresses));
        }

        return sensitivity;
    }
}
