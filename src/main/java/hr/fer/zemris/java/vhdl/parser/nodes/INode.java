package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

/**
 * Created by Dominik on 25.7.2016..
 */
public interface INode {
	void accept(IVisitor visitor);
}
