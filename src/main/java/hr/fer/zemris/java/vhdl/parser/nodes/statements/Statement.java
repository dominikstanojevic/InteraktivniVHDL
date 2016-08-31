package hr.fer.zemris.java.vhdl.parser.nodes.statements;

/**
 * Created by Dominik on 22.8.2016..
 */
public abstract class Statement {
	private String label;

	public Statement(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
