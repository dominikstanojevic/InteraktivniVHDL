package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ProgramNode implements INode {
	private List<INode> children;

	public void addChild(INode node) {
		if(children == null) {
			children = new ArrayList<>();
		}

		children.add(node);
	}

	public List<INode> getChildren() {
		return children;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitProgramNode(this);
	}
}
