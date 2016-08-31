package hr.fer.zemris.java.vhdl.models.declarations;

import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.Objects;

/**
 * Created by Dominik on 25.8.2016..
 */
public class SignalDeclaration implements Declaration {
	private Value.TypeOf typeOf;

	private Integer start;
	private Vector.Order order;
	private Integer end;

	private static SignalDeclaration logicDeclaration = new SignalDeclaration(Value.TypeOf
			.STD_LOGIC, null, null, null);

	public static SignalDeclaration getLogicDeclaration() {
		return logicDeclaration;
	}

	private SignalDeclaration(
			Value.TypeOf typeOf, Integer start, Vector.Order order, Integer end) {
		this.typeOf = typeOf;
		this.start = start;
		this.order = order;
		this.end = end;
	}

	public SignalDeclaration(int start, Vector.Order order, int end) {
		this(Value.TypeOf.STD_LOGIC_VECTOR, start, order, end);

		Objects.requireNonNull(order, "Vector order cannot be null.");
	}

	public Value.TypeOf getTypeOf() {
		return typeOf;
	}

	@Override
	public int size() {
		return Math.abs(end - start) + 1;
	}

	@Override
	public Declaration convertToScalar() {
		return SignalDeclaration.getLogicDeclaration();
	}

	public Integer getStart() {
		return start;
	}

	public Vector.Order getOrder() {
		return order;
	}

	public Integer getEnd() {
		return end;
	}
}
