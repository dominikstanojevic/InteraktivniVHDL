package hr.fer.zemris.java.vhdl.models.values;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Dominik on 29.7.2016..
 */
public class Vector implements Value {
	@Override
	public boolean equals(Value value) {
		if (!(value instanceof Vector)) {
			return false;
		}

		if (this.values.length != ((Vector) value).values.length) {
			return false;
		}

		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals(((Vector) value).values[i])) {
				return false;
			}
		}

		return true;
	}

	public enum Order {TO, DOWNTO}

	private LogicValue[] values;
	private Order order;
	private int start;

	public Vector(int start, Order order, int end) {
		if (!((order == Order.TO && end > start) || (order == Order.DOWNTO && start > end))) {
			throw new IllegalArgumentException("Vector size should be size 1 or bigger.");
		}

		values = new LogicValue[Math.abs(end - start)];
		this.order = order;
		this.start = start;
		Arrays.fill(values, LogicValue.UNINITIALIZED);
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
		checkPosition(position);

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
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append('"');
		for (LogicValue value : values) {
			sb.append(value.toString());
		}
		sb.append('"');

		return sb.toString();
	}
}
