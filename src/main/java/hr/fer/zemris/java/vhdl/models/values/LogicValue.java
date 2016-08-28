package hr.fer.zemris.java.vhdl.models.values;

import java.util.NoSuchElementException;

/**
 * Created by Dominik on 28.7.2016..
 */
public enum LogicValue implements Value {
	ZERO("0"), ONE("1"), UNINITIALIZED("U");

	public Value.TypeOf typeOf() {
		return TypeOf.STD_LOGIC;
	}

	private String representation;

	LogicValue(String s) {
		this.representation = s;
	}

	public static LogicValue getValue(char value) {
		switch (Character.toLowerCase(value)) {
			case '0':
				return ZERO;
			case '1':
				return ONE;
			case 'u':
				return UNINITIALIZED;
			default:
				throw new NoSuchElementException("Invalid character for logic value.");
		}
	}

	@Override
	public String toString() {
		return this.representation;
	}
}
