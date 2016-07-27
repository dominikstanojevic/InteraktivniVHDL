package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.executor.IVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class EntityNode implements INode {
	private String name;
	private List<DeclarationNode> declarations;
	private String end;

	public EntityNode(String name) {
		this.name = name;
	}

	public void addDeclarationNode(DeclarationNode node) {
		if(declarations == null) {
			declarations = new ArrayList<>();
		}

		declarations.add(node);
	}

	public String getName() {
		return name;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public List<DeclarationNode> getDeclarations() {
		return declarations;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitEntityNode(this);
	}
}
