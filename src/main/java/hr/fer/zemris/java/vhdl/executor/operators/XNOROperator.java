package hr.fer.zemris.java.vhdl.executor.operators;

/**
 * Created by Dominik on 27.7.2016..
 */
public class XNOROperator implements BinaryOperator {
	@Override
	public Boolean compute(Boolean firstOperand, Boolean secondOperand) {
		return Operations.not(Operations.xor(firstOperand, secondOperand));
	}
}
