package hr.fer.zemris.java.vhdl.parser.nodes.expressions.unary;

import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.ParserException;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalExpression;

import java.util.Objects;

/**
 * Created by Dominik on 7.8.2016..
 */
public class IndexerOperator extends UnaryOperator {
	private SignalExpression signal;
	private int position;

	public IndexerOperator(SignalExpression signal, int position) {
		super(signal);

		Objects.requireNonNull(signal, "Vector cannot be null");
		this.signal = signal;
		this.position = position;
	}

	public String getSignal() {
		return signal.getId();
	}

	public int getPosition() {
		return position;
	}

	@Override
	public Value evaluate(Table table, String label) {
		Value value = expression.evaluate(table, label);

		if(value instanceof Vector) {
			return ((Vector) value).getLogicValue(position);
		}

		throw new ParserException("Vector expected in indexer operator.");
	}
}
