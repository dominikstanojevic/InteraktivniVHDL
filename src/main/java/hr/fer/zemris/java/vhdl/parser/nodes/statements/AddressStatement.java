package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Set;

/**
 * Created by Dominik on 22.2.2017..
 */
public class AddressStatement {
    private VectorData address;
    private Expression expression;
    private Set<Integer> sensitivity;
    private long delay;

    public AddressStatement(VectorData address, Expression expression, Set<Integer> sensitivity, long delay) {
        this.address = address;
        this.expression = expression;
        this.sensitivity = sensitivity;
        this.delay = delay;
    }
}
