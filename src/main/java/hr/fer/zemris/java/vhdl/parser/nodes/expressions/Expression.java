package hr.fer.zemris.java.vhdl.parser.nodes.expressions;

import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.Value;

/**
 * Created by Dominik on 29.7.2016..
 */
public abstract class Expression {
	public abstract Value evaluate();
	public abstract Declaration getDeclaration();
	public abstract boolean isValid();
	public abstract int valueSize();

	public abstract Expression prepareExpression(Model model);
}
