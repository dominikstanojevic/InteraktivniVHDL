package hr.fer.zemris.java.vhdl.models.declarations;

import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public class PortDeclaration implements Declaration {

	public SignalDeclaration convertToSignalDeclaration() {
		if(typeOf == Value.TypeOf.STD_LOGIC) {
			return SignalDeclaration.getLogicDeclaration();
		} else {
			return new SignalDeclaration(start, order, end);
		}
	}

	public enum Type {
		IN, OUT
	}

	private static Map<Type, PortDeclaration> logicDeclarations = new HashMap<>();
	static {
		logicDeclarations.put(Type.IN, new PortDeclaration(Type.IN));
		logicDeclarations.put(Type.OUT, new PortDeclaration(Type.OUT));
	}

	private Type portType;
	private Value.TypeOf typeOf;
	private Integer start;
	private Vector.Order order;
	private Integer end;

	public static PortDeclaration getLogicDeclaration(Type type) {
		Objects.requireNonNull(type, "Type cannot be null");

		return logicDeclarations.get(type);
	}

	private PortDeclaration(Type type) {
		this(type, Value.TypeOf.STD_LOGIC, null, null, null);
	}

	public PortDeclaration(Type type, int start, Vector.Order order, int end) {
		this(type, Value.TypeOf.STD_LOGIC_VECTOR, start, order, end);

		Objects.requireNonNull(order, "Order cannot be null.");
	}

	private PortDeclaration(
			Type type, Value.TypeOf typeOf, Integer start, Vector.Order order, Integer end) {
		Objects.requireNonNull(type, "Type cannot be null.");

		this.portType = type;
		this.typeOf = typeOf;
		this.start = start;
		this.order = order;
		this.end = end;
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
		return PortDeclaration.getLogicDeclaration(this.portType);
	}

	public Type getPortType() {
		return portType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PortDeclaration that = (PortDeclaration) o;

		if (portType != that.portType) {
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
		int result = portType.hashCode();
		result = 31 * result + typeOf.hashCode();
		result = 31 * result + (start != null ? start.hashCode() : 0);
		result = 31 * result + (order != null ? order.hashCode() : 0);
		result = 31 * result + (end != null ? end.hashCode() : 0);
		return result;
	}
}
