package hr.fer.zemris.java.vhdl.executor.operators;

/**
 * Created by Dominik on 27.7.2016..
 */
public class XOROperator implements BinaryOperator {
	@Override
	public Boolean compute(Boolean firstOperand, Boolean secondOperand) {
		if (firstOperand == null || secondOperand == null) {
			return null;
		}

		return firstOperand ^ secondOperand;
	}
}
