package hr.fer.zemris.java.vhdl.executor.operators;

/**
 * Created by Dominik on 27.7.2016..
 */
public class OROperator implements BinaryOperator {
	@Override
	public Boolean compute(Boolean firstOperand, Boolean secondOperand) {
		return Operations.or(firstOperand, secondOperand);
	}
}
