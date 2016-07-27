package hr.fer.zemris.java.vhdl.executor.operators;

/**
 * Created by Dominik on 27.7.2016..
 */
public interface BinaryOperator extends IOperator {
	public Boolean compute(Boolean firstOperand, Boolean secondOperand);
}
