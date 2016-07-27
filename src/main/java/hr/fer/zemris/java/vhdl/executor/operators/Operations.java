package hr.fer.zemris.java.vhdl.executor.operators;

/**
 * Created by Dominik on 27.7.2016..
 */
public class Operations {

	static Boolean not(Boolean operand) {
		if (operand == null) {
			return null;
		}

		return !operand;
	}

	static Boolean or(Boolean firstOperand, Boolean secondOperand) {
		if (firstOperand == null && secondOperand == null) {
			return null;
		}

		if (firstOperand == null) {
			if (secondOperand == false) {
				return null;
			}
			return true;
		}

		if (secondOperand == null) {
			if (firstOperand == false) {
				return null;
			}
			return true;
		}

		return firstOperand || secondOperand;
	}

	static Boolean and(Boolean firstOperand, Boolean secondOperand) {
		if (firstOperand == null && secondOperand == null) {
			return null;
		}

		if (firstOperand == null) {
			if (secondOperand == true) {
				return null;
			}
			return false;
		}

		if (secondOperand == null) {
			if (firstOperand == true) {
				return null;
			}
			return false;
		}

		return firstOperand && secondOperand;
	}

	static Boolean xor(Boolean firstOperand, Boolean secondOperand) {
		if (firstOperand == null || secondOperand == null) {
			return null;
		}

		return firstOperand ^ secondOperand;
	}
}
