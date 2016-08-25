package hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal;

import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public class SignalDeclaration {

	public enum Type {
		IN, OUT, INTERNAL
	}

	private static Map<Type, SignalDeclaration> logicDeclarations = new HashMap<>();
	static {
		logicDeclarations.put(Type.IN, new SignalDeclaration(Type.IN));
		logicDeclarations.put(Type.OUT, new SignalDeclaration(Type.OUT));
		logicDeclarations.put(Type.INTERNAL, new SignalDeclaration(Type.INTERNAL));
	}

	private Type signalType;
	private Class typeOf;
	private Integer start;
	private Vector.Order order;
	private Integer end;

	public static SignalDeclaration getLogicDeclaration(Type type) {
		Objects.requireNonNull(type, "Type cannot be null");

		return logicDeclarations.get(type);
	}

	private SignalDeclaration(Type type) {
		this(type, LogicValue.class, null, null, null);
	}

	public SignalDeclaration(Type type, int start, Vector.Order order, int end) {
		this(type, Vector.class, start, order, end);

		Objects.requireNonNull(order, "Order cannot be null.");
	}

	private SignalDeclaration(
			Type type, Class typeOf, Integer start, Vector.Order order, Integer end) {
		Objects.requireNonNull(type, "Type cannot be null.");

		this.signalType = type;
		this.typeOf = typeOf;
		this.start = start;
		this.order = order;
		this.end = end;
	}

	public Class getTypeOf() {
		return typeOf;
	}

	public Type getSignalType() {
		return signalType;
	}

	public static SignalDeclaration changeType(SignalDeclaration declaration, Type type) {
		return new SignalDeclaration(type, declaration.typeOf, declaration.start,
				declaration.order, declaration.end);
	}

	public static boolean checkMapping(
			SignalDeclaration d1, SignalDeclaration d2) {

		if ((d1 == null && d2.getSignalType() == Type.OUT) ||
			(d2 == null && d1.getSignalType() == Type.OUT)) {
			return true;
		} else if (d1 == null || d2 == null) {
			return false;
		}

		if (d1.typeOf != d2.typeOf) {
			return false;
		}

		if (d1.typeOf == Vector.class) {
			if (Math.abs(d1.end - d1.start) != Math.abs(d2.end - d2.start)) {
				return false;
			}
		}

		if ((d1.signalType == Type.IN && d2.signalType == Type.OUT) || (
				d1.signalType == Type.OUT && d2.signalType == Type.IN)) {
			return false;
		}

		return true;
	}

	public Value createValue() {
		if (typeOf == LogicValue.class) {
			return LogicValue.UNINITIALIZED;
		} else {
			return new Vector(start, order, end);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SignalDeclaration that = (SignalDeclaration) o;

		if (signalType != that.signalType) {
			return false;
		}
		if (!typeOf.equals(that.typeOf)) {
			return false;
		}
		if (start != null ? !start.equals(that.start) : that.start != null) {
			return false;
		}
		if (order != that.order) {
			return false;
		}
		return end != null ? end.equals(that.end) : that.end == null;

	}

	@Override
	public int hashCode() {
		int result = signalType.hashCode();
		result = 31 * result + typeOf.hashCode();
		result = 31 * result + (start != null ? start.hashCode() : 0);
		result = 31 * result + (order != null ? order.hashCode() : 0);
		result = 31 * result + (end != null ? end.hashCode() : 0);
		return result;
	}
}
