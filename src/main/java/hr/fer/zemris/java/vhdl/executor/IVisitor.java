package hr.fer.zemris.java.vhdl.executor;

import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ExpressionNode;
import hr.fer.zemris.java.vhdl.parser.nodes.InputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.OutputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;

/**
 * Created by Dominik on 27.7.2016..
 */
public interface IVisitor {
	void visitProgramNode(ProgramNode node);
	void visitEntityNode(EntityNode node);
	void visitInputNode(InputNode node);
	void visitOutputNode(OutputNode node);
	void visitArchNode(ArchitectureNode node);
	void visitExpressionNode(ExpressionNode node);
}
