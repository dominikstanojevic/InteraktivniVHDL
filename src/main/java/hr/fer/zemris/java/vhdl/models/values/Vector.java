package hr.fer.zemris.java.vhdl.models.values;

import hr.fer.zemris.java.vhdl.models.declarations.SignalDeclaration;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public class Vector implements Value {

	@Override
	public TypeOf typeOf() {
		return TypeOf.STD_LOGIC_VECTOR;
	}

	@Override
	public SignalDeclaration getDeclaration() {
		int end;
		if(order == Order.TO) {
			end = start + values.length - 1;
		} else {
			end = start - values.length + 1;
		}

		return new SignalDeclaration(start, order, end);
	}

	@Override
	public LogicValue[] getValues() {
		return Arrays.copyOf(values, values.length);
	}

	public static Value concat(Value first, Value second) {
		int size = first.getDeclaration().size() + second.getDeclaration().size();
		LogicValue[] values = new LogicValue[size];

		LogicValue[] firstValues = first.getValues();
		LogicValue[] secondValues = second.getValues();

		System.arraycopy(firstValues, 0, values, 0, firstValues.length);
		System.arraycopy(secondValues, 0, values, firstValues.length, secondValues.length);

		return new Vector(values);
	}

	public void setValue(Vector value) {
		this.values = value.values;
	}

	public enum Order {TO, DOWNTO}

	private LogicValue[] values;
	private Order order;
	private int start;

	public Vector(int start, Order order, int end) {
		if (!((order == Order.TO && end >= start) ||
			  (order == Order.DOWNTO && start >= end))) {
			throw new IllegalArgumentException("Vector size should be size 1 or bigger.");
		}

		values = new LogicValue[Math.abs(end - start) + 1];
		this.order = order;
		this.start = start;
		Arrays.fill(values, LogicValue.UNINITIALIZED);
	}

	public static Vector createVector(char[] values) {
		Objects.requireNonNull(values, "Values cannot be null");

		LogicValue[] logicValues = new LogicValue[values.length];
		for(int i = 0; i < logicValues.length; i++) {
			logicValues[i] = LogicValue.getValue(values[i]);
		}

		return new Vector(logicValues);
	}

	public Vector(LogicValue[] logicValues) {
		checkVector(logicValues);

		values = Arrays.copyOf(logicValues, logicValues.length);
		order = Order.TO;
		start = 0;
	}

	public int length() {
		return values.length;
	}

	public LogicValue[] getValue() {
		return Arrays.copyOf(values, values.length);
	}

	public LogicValue getLogicValue(int position) {
		position = checkPosition(position);

		return values[position];
	}

	public void setLogicValue(LogicValue value, int position) {
		Objects.requireNonNull(value, "Value should not be null.");
		position = checkPosition(position);

		values[position] = value;
	}

	public int checkPosition(int position) {
		if (order == Order.TO && (position >= start && position < start + values.length)) {
			return position - start;
		}
		if (order == Order.DOWNTO && (position <= start && position > start - values.length)) {
			return start - position;
		}

		throw new IllegalArgumentException("Invalid position for this vector.");
	}

	private void checkVector(Value[] vector) {
		Objects.requireNonNull(vector, "Logic vector cannot be null.");
		for (Value value : vector) {
			Objects.requireNonNull(value, "StdLogic value in vector cannot be null");
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

		Vector vector = (Vector) o;

		if (start != vector.start) {
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(values, vector.values)) {
			return false;
		}
		return order == vector.order;

	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(values);
		result = 31 * result + order.hashCode();
		result = 31 * result + start;
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append('"');
		for (LogicValue value : values) {
			sb.append(value.toString());
		}
		sb.append('"');

		return sb.toString();
	}

	public static class VectorData {
		private int start;
		private Order order;
		private int end;

		public VectorData(int start, Order order, int end) {
			this.start = start;
			this.order = order;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public Order getOrder() {
			return order;
		}

		public int getEnd() {
			return end;
		}
	}
}
