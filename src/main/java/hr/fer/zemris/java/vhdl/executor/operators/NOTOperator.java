package hr.fer.zemris.java.vhdl.executor.operators;

/**
 * Created by Dominik on 27.7.2016..
 */
public class NOTOperator implements UnaryOperator {
	@Override
	public Boolean compute(Boolean operand) {
		if(operand == null) {
			return null;
		}

		return !operand;
	}
}
