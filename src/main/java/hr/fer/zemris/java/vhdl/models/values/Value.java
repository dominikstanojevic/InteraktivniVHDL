package hr.fer.zemris.java.vhdl.models.values;

import hr.fer.zemris.java.vhdl.models.declarations.SignalDeclaration;

import java.util.function.Function;

/**
 * Created by Dominik on 29.7.2016..
 */
public interface Value {
	enum TypeOf {
		STD_LOGIC((declaration) -> LogicValue.UNINITIALIZED),
		STD_LOGIC_VECTOR(d -> new Vector(d.getStart(), d.getOrder(), d.getEnd()));

		private Function<SignalDeclaration, Value> function;

		TypeOf(Function<SignalDeclaration, Value> function) {
			this.function = function;
		}

		public Value getDefaultValue(SignalDeclaration declaration) {
			return this.function.apply(declaration);
		}
	}

	TypeOf typeOf();
}
