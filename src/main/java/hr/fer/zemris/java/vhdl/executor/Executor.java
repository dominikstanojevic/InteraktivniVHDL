package hr.fer.zemris.java.vhdl.executor;

import hr.fer.zemris.java.vhdl.executor.operators.ANDOperator;
import hr.fer.zemris.java.vhdl.executor.operators.IOperator;
import hr.fer.zemris.java.vhdl.executor.operators.NANDOperator;
import hr.fer.zemris.java.vhdl.executor.operators.NOROperator;
import hr.fer.zemris.java.vhdl.executor.operators.NOTOperator;
import hr.fer.zemris.java.vhdl.executor.operators.OROperator;
import hr.fer.zemris.java.vhdl.executor.operators.XOROperator;
import hr.fer.zemris.java.vhdl.models.Architecture;
import hr.fer.zemris.java.vhdl.models.Entity;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ExpressionNode;
import hr.fer.zemris.java.vhdl.parser.nodes.InputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.OutputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dominik on 27.7.2016..
 */
public class Executor {
	private static Map<String, IOperator> operators = new HashMap<>();
	static {
		operators.put("not", new NOTOperator());
		operators.put("and", new ANDOperator());
		operators.put("or", new OROperator());
		operators.put("xor", new XOROperator());
		operators.put("nand", new NANDOperator());
		operators.put("nor", new NOROperator());
	}

	private Entity entity;
	private List<Architecture> architectures;

	public class Visitor implements IVisitor {

		@Override
		public void visitProgramNode(ProgramNode node) {

		}

		@Override
		public void visitEntityNode(EntityNode node) {

		}

		@Override
		public void visitInputNode(InputNode node) {

		}

		@Override
		public void visitOutputNode(OutputNode node) {

		}

		@Override
		public void visitArchNode(ArchitectureNode node) {

		}

		@Override
		public void visitExpressionNode(ExpressionNode node) {

		}
	}
}
