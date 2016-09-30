package hr.fer.zemris.java.vhdl.models.declarable;

import hr.fer.zemris.java.vhdl.models.declarations.SignalDeclaration;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class Signal implements Declarable<SignalDeclaration> {
	private String label;
	private SignalDeclaration declaration;
	private Value value;

	public Signal(String label, SignalDeclaration declaration) {
		Objects.requireNonNull(label, "Signal label cannot be null.");
		Objects.requireNonNull(declaration, "Signal declaration cannot be null");

		this.label = label;
		this.declaration = declaration;
	}

	public Signal(Value value) {
		this.label = value.toString();
		this.declaration = value.getDeclaration();
		this.value = value;
	}

	public Signal createValue(String label) {
		if (value == null) {
			String prefix = "";
			if(!label.isEmpty()) {
				prefix = label + "/";
			}

			Signal newSignal = new Signal(prefix + this.label, declaration);
			newSignal.value = declaration.getTypeOf().getDefaultValue(declaration);
			return newSignal;
		} else {
			throw new RuntimeException("Value already created.");
		}
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		if(this.value.typeOf() != value.typeOf()) {
			throw new RuntimeException("Illegal value for signal: " + label);
		}

		if(value.typeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
			if(((Vector) value).length() != ((Vector) this.value).length()) {
				throw new RuntimeException("Illegal size for signal: " + label);
			}

			((Vector) this.value).setValue((Vector) value);
		} else {
			this.value = value;
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

		Signal signal = (Signal) o;

		return label.equals(signal.label);

	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		return label.hashCode();
	}

	@Override
	public DeclarationType getDeclarationType() {
		return DeclarationType.SIGNAL;
	}

	@Override
	public String getName() {
		return label;
	}

	@Override
	public SignalDeclaration getDeclaration() {
		return declaration;
	}
}
