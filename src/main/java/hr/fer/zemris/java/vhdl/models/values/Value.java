package hr.fer.zemris.java.vhdl.models.values;

/**
 * Created by Dominik on 29.7.2016..
 */
public interface Value {
	enum TypeOf {
		STD_LOGIC, STD_LOGIC_VECTOR
	}

	boolean equals(Value value);
}
