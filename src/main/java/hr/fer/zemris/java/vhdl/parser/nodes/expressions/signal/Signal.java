package hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public class Signal extends Expression {
	@Override
	public Value evaluate() {
		return value;
	}

	public enum Type {
		IN, OUT, INTERNAL
	}

	private Type signalType;
	protected String id;
	protected Value value;
	private Class typeOf;

	public Signal(String id, Value value, Type type) {
		Objects.requireNonNull(id, "Identifier cannot be null.");
		Objects.requireNonNull(value, "Value cannot be null");
		Objects.requireNonNull(type, "Type cannot be null.");

		this.id = id;
		this.value = value;
		this.signalType = type;

		if (value instanceof LogicValue) {
			this.typeOf = ((LogicValue) value).getDeclaringClass();
		} else {
			this.typeOf = value.getClass();
		}
	}

	public Class getTypeOf() {
		return typeOf;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		Objects.requireNonNull(value, "Value cannot be null.");
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public Type getSignalType() {
		return signalType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Signal signal = (Signal) o;

		return id.equals(signal.id);

	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
