package hr.fer.zemris.java.vhdl.executor.operators;

/**
 * Created by Dominik on 27.7.2016..
 */
public class ANDOperator implements BinaryOperator {
	@Override
	public Boolean compute(Boolean firstOperand, Boolean secondOperand) {
		return Operations.and(firstOperand, secondOperand);
	}
}
