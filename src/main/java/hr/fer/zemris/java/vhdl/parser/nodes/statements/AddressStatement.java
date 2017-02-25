package hr.fer.zemris.java.vhdl.parser.nodes.statements;

import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Set;

/**
 * Created by Dominik on 22.2.2017..
 */
public class AddressStatement {
    private Integer[] address;
    private Expression expression;
    private Set<Integer> sensitivity;
    private long delay;

    public AddressStatement(Integer[] addresses, Expression expression, Set<Integer> sensitivity, long delay) {
        this.address = addresses;
        this.expression = expression;
        this.sensitivity = sensitivity;
        this.delay = delay;
    }

    public Integer[] getAddress() {
        return address;
    }

    public Expression getExpression() {
        return expression;
    }

    public Set<Integer> getSensitivity() {
        return sensitivity;
    }

    public long getDelay() {
        return delay;
    }
}
