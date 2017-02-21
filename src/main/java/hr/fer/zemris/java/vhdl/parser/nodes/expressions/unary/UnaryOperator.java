package hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public abstract class UnaryOperator extends Expression {
	protected Expression expression;

	public UnaryOperator(Expression expression) {
		Objects.requireNonNull(expression, "Expression cannot be null.");
		this.expression = expression;
	}

	@Override
	public Declaration getDeclaration() {
		return expression.getDeclaration();
	}

	@Override
	public boolean isValid() {
		return expression.isValid();
	}

	@Override
	public int valueSize() {
		return expression.valueSize();
	}
}
