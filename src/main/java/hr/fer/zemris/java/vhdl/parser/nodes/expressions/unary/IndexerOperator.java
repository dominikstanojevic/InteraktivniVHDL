package hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Declarable;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.ParserException;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalExpression;

/**
 * Created by Dominik on 7.8.2016..
 */
public class IndexerOperator extends SignalExpression{
	private int position;

	public IndexerOperator(Declarable id, int position) {
		super(id);

		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public Value evaluate(Table table, String label) {
		Value value = super.evaluate(table, label);

		if(value instanceof Vector) {
			return ((Vector) value).getLogicValue(position);
		}

		throw new ParserException("Vector expected in indexer operator.");
	}

	@Override
	public Declaration getDeclaration() {
		return super.getDeclaration().convertToScalar();
	}
}
