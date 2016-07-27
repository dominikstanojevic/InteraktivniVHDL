package hr.fer.zemris.java.vhdl.parser.nodes;

/**
 * Created by Dominik on 27.7.2016..
 */
public class Constant implements IExpressionElement {
	private Character value;

	public Constant(Character value) {
		this.value = value;
	}

	public Boolean getConstant() throws InvalidConstantException {
		if(value == '0') return false;
		if(value == '1') return true;
		if(value == 'u') return null;

		throw new InvalidConstantException(value.toString());
	}
}
