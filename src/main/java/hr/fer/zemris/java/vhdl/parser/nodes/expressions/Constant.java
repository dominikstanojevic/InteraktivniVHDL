package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.SignalDeclaration;
import hr.fer.zemris.java.vhdl.models.mappers.Mappable;
import hr.fer.zemris.java.vhdl.models.values.Value;

/**
 * Created by Dominik on 29.7.2016..
 */
public class Constant extends Expression implements Mappable {
	private Value value;
	private SignalDeclaration declaration;

	public Constant(Value value) {
		this.value = value;
	}

	public Value getValue() {
		return value;
	}

	@Override
	public Value evaluate(Table table, String label) {
		return value;
	}

	@Override
	public String getName() {
		return value.toString();
	}

	@Override
	public Declaration getDeclaration() {
		if(declaration == null) {
			declaration = value.getDeclaration();
		}

		return declaration;
	}
}
