package hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Declarable;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.mappers.Mappable;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;

import java.util.Objects;

/**
 * Created by Dominik on 22.8.2016..
 */
public class SignalExpression extends Expression implements Mappable {
	protected Declarable id;

	public SignalExpression(Declarable id) {
		Objects.requireNonNull(id, "Signal id cannot be null");

		this.id = id;
	}

	public Declarable getId() {
		return id;
	}

	@Override
	public String getName() {
		return id.getName();
	}

	@Override
	public Declaration getDeclaration() {
		return id.getDeclaration();
	}


	@Override
	public Value evaluate(Table table, String componentName) {
		return table.getValueForSignal(componentName, id.getName());
	}

	@Override
	public String toString() {
		return "/" + id.getName();
	}
}
